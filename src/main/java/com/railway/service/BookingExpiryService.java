package com.railway.service;

import com.railway.model.Booking;
import com.railway.model.BookingStatus;
import com.railway.model.SeatInventory;
import com.railway.repository.BookingRepository;
import com.railway.repository.SeatInventoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingExpiryService {

    private final BookingRepository bookingRepository;
    private final SeatInventoryRepository seatInventoryRepository;
    private final BookingService bookingService; // Reuse logic for cascade if needed, or implement here

    @Scheduled(fixedRate = 60000)
    @Transactional
    public void expireUnpaidBookings() {
        LocalDateTime now = LocalDateTime.now();
        List<BookingStatus> statuses = Arrays.asList(BookingStatus.CONFIRMED, BookingStatus.RAC, BookingStatus.WAITLISTED);
        List<Booking> expiredBookings = bookingRepository.findByPaidFalseAndHoldExpiresAtBeforeAndStatusIn(now, statuses);

        for (Booking booking : expiredBookings) {
            try {
                SeatInventory si = seatInventoryRepository.findByTrainRunAndSeatClassWithLock(booking.getTrainRun(), booking.getSeatClass())
                        .orElseThrow();
                        
                int passengerCount = booking.getPassengers().size();

                if (booking.getStatus() == BookingStatus.CONFIRMED) {
                    si.setConfirmedBooked(si.getConfirmedBooked() - passengerCount);
                    promoteRacToConfirmed(booking, si, passengerCount);
                } else if (booking.getStatus() == BookingStatus.RAC) {
                    si.setRacBooked(si.getRacBooked() - passengerCount);
                    promoteWaitlistToRac(booking, si, passengerCount);
                } else if (booking.getStatus() == BookingStatus.WAITLISTED) {
                    si.setWaitlistCount(si.getWaitlistCount() - passengerCount);
                    recalculateWaitlistPositions(booking);
                }

                booking.setStatus(BookingStatus.EXPIRED);
                booking.setUpdatedAt(now);
                seatInventoryRepository.save(si);
                bookingRepository.save(booking);
                
            } catch (Exception e) {
                log.error("Failed to expire booking: " + booking.getPnr(), e);
            }
        }
        
        if (!expiredBookings.isEmpty()) {
            log.info("Expired {} unpaid bookings", expiredBookings.size());
        }
    }

    // Simplified copy of cascade for autonomy
    private void promoteRacToConfirmed(Booking booking, SeatInventory si, int slotsAvailable) {
        for (int i = 0; i < slotsAvailable; i++) {
            Optional<Booking> racOpt = bookingRepository.findFirstByTrainRunAndSeatClassAndStatusOrderByCreatedAtAsc(booking.getTrainRun(), booking.getSeatClass(), BookingStatus.RAC);
            if (racOpt.isPresent() && racOpt.get().getPassengers().size() <= slotsAvailable - i) {
                Booking racBooking = racOpt.get();
                racBooking.setStatus(BookingStatus.CONFIRMED);
                bookingRepository.save(racBooking);
                si.setRacBooked(si.getRacBooked() - racBooking.getPassengers().size());
                si.setConfirmedBooked(si.getConfirmedBooked() + racBooking.getPassengers().size());
                i += (racBooking.getPassengers().size() - 1);
                promoteWaitlistToRac(booking, si, racBooking.getPassengers().size());
            }
        }
    }

    private void promoteWaitlistToRac(Booking booking, SeatInventory si, int slotsAvailable) {
        for (int i = 0; i < slotsAvailable; i++) {
            Optional<Booking> wlOpt = bookingRepository.findFirstByTrainRunAndSeatClassAndStatusOrderByCreatedAtAsc(booking.getTrainRun(), booking.getSeatClass(), BookingStatus.WAITLISTED);
            if (wlOpt.isPresent() && wlOpt.get().getPassengers().size() <= slotsAvailable - i) {
                Booking wlBooking = wlOpt.get();
                wlBooking.setStatus(BookingStatus.RAC);
                wlBooking.setWaitlistPosition(null);
                bookingRepository.save(wlBooking);
                si.setWaitlistCount(si.getWaitlistCount() - wlBooking.getPassengers().size());
                si.setRacBooked(si.getRacBooked() + wlBooking.getPassengers().size());
                i += (wlBooking.getPassengers().size() - 1);
            }
        }
        recalculateWaitlistPositions(booking);
    }

    private void recalculateWaitlistPositions(Booking booking) {
        List<Booking> waitlisted = bookingRepository.findByTrainRunAndSeatClassAndStatusOrderByWaitlistPositionAsc(booking.getTrainRun(), booking.getSeatClass(), BookingStatus.WAITLISTED);
        int pos = 1;
        for (Booking b : waitlisted) {
            b.setWaitlistPosition(pos);
            pos += b.getPassengers().size();
            bookingRepository.save(b);
        }
    }
}
