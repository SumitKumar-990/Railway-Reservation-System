package com.railway.service;

import com.railway.dto.booking.BookingRequest;
import com.railway.dto.booking.BookingResponse;
import com.railway.dto.booking.PassengerRequest;
import com.railway.dto.booking.PassengerResponse;
import com.railway.exception.BookingException;
import com.railway.model.*;
import com.railway.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingService {

    private final TrainRepository trainRepository;
    private final StationRepository stationRepository;
    private final SeatClassRepository seatClassRepository;
    private final TrainRunRepository trainRunRepository;
    private final TrainStopRepository trainStopRepository;
    private final TrainClassConfigRepository trainClassConfigRepository;
    private final SeatInventoryRepository seatInventoryRepository;
    private final BookingRepository bookingRepository;
    private final FareCalculator fareCalculator;
    private final QrCodeService qrCodeService;
    private final PaymentRepository paymentRepository;

    @Value("${app.booking.hold-minutes:15}")
    private int holdMinutes;

    @Transactional
    public BookingResponse createBooking(BookingRequest request, User user) {
        Train train = trainRepository.findByTrainNumber(request.getTrainNumber())
                .orElseThrow(() -> new BookingException("Train not found"));
        Station fromStation = stationRepository.findByCode(request.getFromStationCode())
                .orElseThrow(() -> new BookingException("From station not found"));
        Station toStation = stationRepository.findByCode(request.getToStationCode())
                .orElseThrow(() -> new BookingException("To station not found"));
        SeatClass seatClass = seatClassRepository.findByCode(request.getSeatClassCode())
                .orElseThrow(() -> new BookingException("Seat class not found"));

        TrainRun trainRun = getOrCreateTrainRun(train, request.getDate());

        TrainStop fromStop = trainStopRepository.findByTrainAndStation(train, fromStation)
                .orElseThrow(() -> new BookingException("Train doesn't stop at from station"));
        TrainStop toStop = trainStopRepository.findByTrainAndStation(train, toStation)
                .orElseThrow(() -> new BookingException("Train doesn't stop at to station"));

        if (fromStop.getSequenceNumber() >= toStop.getSequenceNumber()) {
            throw new BookingException("Invalid route selection");
        }

        TrainClassConfig config = trainClassConfigRepository.findByTrainAndSeatClass(train, seatClass)
                .orElseThrow(() -> new BookingException("Seat class not available on this train"));

        double fare = fareCalculator.calculateFare(config, fromStop, toStop, request.getPassengers().size());

        // PESSIMISTIC_WRITE lock
        SeatInventory si = seatInventoryRepository.findByTrainRunAndSeatClassWithLock(trainRun, seatClass)
                .orElseThrow(() -> new BookingException("Seat inventory not found"));

        BookingStatus status;
        Integer waitlistPosition = null;

        int passengerCount = request.getPassengers().size();

        if (si.availableConfirmed() >= passengerCount) {
            status = BookingStatus.CONFIRMED;
            si.setConfirmedBooked(si.getConfirmedBooked() + passengerCount);
        } else if (si.availableRac() >= passengerCount) {
            status = BookingStatus.RAC;
            si.setRacBooked(si.getRacBooked() + passengerCount);
        } else {
            status = BookingStatus.WAITLISTED;
            si.setWaitlistCount(si.getWaitlistCount() + passengerCount);
            waitlistPosition = si.getWaitlistCount(); // Simple, assuming all together
        }

        seatInventoryRepository.save(si);

        String pnr = "PNR" + System.currentTimeMillis() + String.format("%04d", (int)(Math.random() * 10000));

        Booking booking = new Booking();
        booking.setPnr(pnr);
        booking.setUser(user);
        booking.setTrainRun(trainRun);
        booking.setFromStation(fromStation);
        booking.setToStation(toStation);
        booking.setSeatClass(seatClass);
        booking.setStatus(status);
        booking.setTotalFare(fare);
        booking.setPaid(false);
        booking.setHoldExpiresAt(LocalDateTime.now().plusMinutes(holdMinutes));
        booking.setWaitlistPosition(waitlistPosition);
        booking.setCreatedAt(LocalDateTime.now());
        booking.setUpdatedAt(LocalDateTime.now());

        List<Passenger> passengers = new ArrayList<>();
        for (PassengerRequest pr : request.getPassengers()) {
            Passenger p = new Passenger();
            p.setBooking(booking);
            p.setName(pr.getName());
            p.setAge(pr.getAge());
            p.setGender(pr.getGender());
            p.setBerthPreference(pr.getBerthPreference());
            passengers.add(p);
        }
        booking.setPassengers(passengers);

        bookingRepository.save(booking);

        return buildBookingResponse(booking);
    }

    @Transactional
    public BookingResponse cancelBooking(String pnr, User user) {
        Booking booking = bookingRepository.findByPnr(pnr)
                .orElseThrow(() -> new BookingException("Booking not found"));

        if (!booking.getUser().getId().equals(user.getId()) && user.getRole() != Role.ADMIN) {
            throw new BookingException("Not authorized to cancel this booking");
        }

        if (booking.getStatus() == BookingStatus.CANCELLED || booking.getStatus() == BookingStatus.EXPIRED) {
            throw new BookingException("Booking is already cancelled or expired");
        }

        SeatInventory si = seatInventoryRepository.findByTrainRunAndSeatClassWithLock(booking.getTrainRun(), booking.getSeatClass())
                .orElseThrow(() -> new BookingException("Seat inventory not found"));

        int passengerCount = booking.getPassengers().size();

        if (booking.getStatus() == BookingStatus.CONFIRMED) {
            si.setConfirmedBooked(si.getConfirmedBooked() - passengerCount);
            promoteRacToConfirmed(booking.getTrainRun(), booking.getSeatClass(), si, passengerCount);
        } else if (booking.getStatus() == BookingStatus.RAC) {
            si.setRacBooked(si.getRacBooked() - passengerCount);
            promoteWaitlistToRac(booking.getTrainRun(), booking.getSeatClass(), si, passengerCount);
        } else if (booking.getStatus() == BookingStatus.WAITLISTED) {
            si.setWaitlistCount(si.getWaitlistCount() - passengerCount);
            recalculateWaitlistPositions(booking.getTrainRun(), booking.getSeatClass());
        }

        booking.setStatus(BookingStatus.CANCELLED);
        booking.setUpdatedAt(LocalDateTime.now());
        
        seatInventoryRepository.save(si);
        bookingRepository.save(booking);

        if (booking.isPaid()) {
            Optional<Payment> optPayment = paymentRepository.findByBooking(booking);
            if (optPayment.isPresent()) {
                Payment payment = optPayment.get();
                payment.setStatus(PaymentStatus.REFUNDED);
                paymentRepository.save(payment);
            }
        }

        return buildBookingResponse(booking);
    }

    @Transactional(readOnly = true)
    public BookingResponse getBooking(String pnr, User user) {
        Booking booking = bookingRepository.findByPnr(pnr)
                .orElseThrow(() -> new BookingException("Booking not found"));

        if (!booking.getUser().getId().equals(user.getId()) && user.getRole() != Role.ADMIN) {
            throw new BookingException("Not authorized to view this booking");
        }

        return buildBookingResponse(booking);
    }

    @Transactional(readOnly = true)
    public List<BookingResponse> getUserBookings(User user) {
        return bookingRepository.findByUserOrderByCreatedAtDesc(user)
                .stream()
                .map(this::buildBookingResponse)
                .collect(Collectors.toList());
    }

    private void promoteRacToConfirmed(TrainRun trainRun, SeatClass seatClass, SeatInventory si, int freedSlots) {
        // Keep promoting RAC bookings as long as there are enough available confirmed seats
        boolean promoted = true;
        while (promoted) {
            promoted = false;
            Optional<Booking> racBookingOpt = bookingRepository.findFirstByTrainRunAndSeatClassAndStatusOrderByCreatedAtAsc(trainRun, seatClass, BookingStatus.RAC);
            if (racBookingOpt.isPresent()) {
                Booking racBooking = racBookingOpt.get();
                int paxCount = racBooking.getPassengers().size();
                if (si.availableConfirmed() >= paxCount) {
                    racBooking.setStatus(BookingStatus.CONFIRMED);
                    bookingRepository.save(racBooking);
                    si.setRacBooked(si.getRacBooked() - paxCount);
                    si.setConfirmedBooked(si.getConfirmedBooked() + paxCount);
                    promoted = true;
                    // After promoting RAC → Confirmed, try to promote WL → RAC for the freed RAC slots
                    promoteWaitlistToRac(trainRun, seatClass, si, paxCount);
                }
            }
        }
    }

    private void promoteWaitlistToRac(TrainRun trainRun, SeatClass seatClass, SeatInventory si, int freedSlots) {
        // Keep promoting waitlisted bookings as long as there are enough available RAC slots
        boolean promoted = true;
        while (promoted) {
            promoted = false;
            Optional<Booking> wlBookingOpt = bookingRepository.findFirstByTrainRunAndSeatClassAndStatusOrderByCreatedAtAsc(trainRun, seatClass, BookingStatus.WAITLISTED);
            if (wlBookingOpt.isPresent()) {
                Booking wlBooking = wlBookingOpt.get();
                int paxCount = wlBooking.getPassengers().size();
                if (si.availableRac() >= paxCount) {
                    wlBooking.setStatus(BookingStatus.RAC);
                    wlBooking.setWaitlistPosition(null);
                    bookingRepository.save(wlBooking);
                    si.setWaitlistCount(si.getWaitlistCount() - paxCount);
                    si.setRacBooked(si.getRacBooked() + paxCount);
                    promoted = true;
                }
            }
        }
        recalculateWaitlistPositions(trainRun, seatClass);
    }

    private void recalculateWaitlistPositions(TrainRun trainRun, SeatClass seatClass) {
        List<Booking> waitlistedBookings = bookingRepository.findByTrainRunAndSeatClassAndStatusOrderByWaitlistPositionAsc(trainRun, seatClass, BookingStatus.WAITLISTED);
        int pos = 1;
        for (Booking b : waitlistedBookings) {
            b.setWaitlistPosition(pos);
            pos += b.getPassengers().size();
            bookingRepository.save(b);
        }
    }

    private BookingResponse buildBookingResponse(Booking booking) {
        String qrCodeDataUri = null;
        if (booking.isBoardable()) {
            qrCodeDataUri = qrCodeService.generateQrCodeDataUri(booking);
        }

        List<PassengerResponse> passResp = booking.getPassengers().stream()
                .map(p -> new PassengerResponse(p.getName(), p.getAge(), p.getGender(), p.getSeatNumber(), p.getBerthPreference() != null ? p.getBerthPreference().name() : null))
                .collect(Collectors.toList());

        TrainStop fromStop = trainStopRepository.findByTrainAndStation(booking.getTrainRun().getTrain(), booking.getFromStation()).orElseThrow();
        TrainStop toStop = trainStopRepository.findByTrainAndStation(booking.getTrainRun().getTrain(), booking.getToStation()).orElseThrow();

        return new BookingResponse(
                booking.getPnr(),
                booking.getTrainRun().getTrain().getTrainNumber(),
                booking.getTrainRun().getTrain().getName(),
                booking.getFromStation().getName(),
                booking.getToStation().getName(),
                booking.getTrainRun().getRunDate(),
                fromStop.getDepartureTime(),
                toStop.getArrivalTime(),
                booking.getSeatClass().getCode(),
                booking.getSeatClass().getLabel(),
                booking.getStatus().name(),
                booking.getTotalFare(),
                booking.isPaid(),
                booking.isBoardable(),
                booking.getWaitlistPosition(),
                booking.getHoldExpiresAt(),
                qrCodeDataUri,
                passResp,
                booking.getCreatedAt()
        );
    }

    private TrainRun getOrCreateTrainRun(Train train, LocalDate date) {
        Optional<TrainRun> existing = trainRunRepository.findByTrainAndRunDate(train, date);
        if (existing.isPresent()) {
            return existing.get();
        }
        
        TrainRun newRun = new TrainRun();
        newRun.setTrain(train);
        newRun.setRunDate(date);
        newRun.setStatus(TrainRunStatus.SCHEDULED);
        trainRunRepository.save(newRun);
        
        List<TrainClassConfig> classConfigs = trainClassConfigRepository.findByTrain(train);
        for (TrainClassConfig config : classConfigs) {
            SeatInventory inventory = new SeatInventory();
            inventory.setTrainRun(newRun);
            inventory.setSeatClass(config.getSeatClass());
            inventory.setTotalSeats(config.getTotalSeats());
            inventory.setConfirmedBooked(0);
            inventory.setRacBooked(0);
            inventory.setRacQuota(config.getRacQuota());
            inventory.setWaitlistCount(0);
            seatInventoryRepository.save(inventory);
        }
        
        return newRun;
    }
}
