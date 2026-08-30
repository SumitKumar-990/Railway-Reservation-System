package com.railway.repository;

import com.railway.model.SeatClass;
import com.railway.model.Train;
import com.railway.model.TrainClassConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TrainClassConfigRepository extends JpaRepository<TrainClassConfig, Long> {
    List<TrainClassConfig> findByTrain(Train train);
    Optional<TrainClassConfig> findByTrainAndSeatClass(Train train, SeatClass seatClass);
}
