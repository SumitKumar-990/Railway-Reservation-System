package com.railway.repository;

import com.railway.model.SeatClass;
import com.railway.model.SeatInventory;
import com.railway.model.TrainRun;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SeatInventoryRepository extends JpaRepository<SeatInventory, Long> {
    Optional<SeatInventory> findByTrainRunAndSeatClass(TrainRun trainRun, SeatClass seatClass);
    List<SeatInventory> findByTrainRun(TrainRun trainRun);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT si FROM SeatInventory si WHERE si.trainRun = :trainRun AND si.seatClass = :seatClass")
    Optional<SeatInventory> findByTrainRunAndSeatClassWithLock(@Param("trainRun") TrainRun trainRun, @Param("seatClass") SeatClass seatClass);
}
