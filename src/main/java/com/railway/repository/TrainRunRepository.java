package com.railway.repository;

import com.railway.model.Train;
import com.railway.model.TrainRun;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface TrainRunRepository extends JpaRepository<TrainRun, Long> {
    Optional<TrainRun> findByTrainAndRunDate(Train train, LocalDate runDate);
}
