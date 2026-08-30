package com.railway.repository;

import com.railway.model.Booking;
import com.railway.model.BookingStatus;
import com.railway.model.SeatClass;
import com.railway.model.TrainRun;
import com.railway.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    Optional<Booking> findByPnr(String pnr);
    List<Booking> findByUserOrderByCreatedAtDesc(User user);
    List<Booking> findByPaidFalseAndHoldExpiresAtBeforeAndStatusIn(LocalDateTime now, List<BookingStatus> statuses);
    Optional<Booking> findFirstByTrainRunAndSeatClassAndStatusOrderByCreatedAtAsc(TrainRun trainRun, SeatClass seatClass, BookingStatus status);
    List<Booking> findByTrainRunAndSeatClassAndStatusOrderByWaitlistPositionAsc(TrainRun trainRun, SeatClass seatClass, BookingStatus status);
}
