package com.railway.repository;

import com.railway.model.Station;
import com.railway.model.Train;
import com.railway.model.TrainStop;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TrainStopRepository extends JpaRepository<TrainStop, Long> {
    List<TrainStop> findByTrainOrderBySequenceNumberAsc(Train train);
    Optional<TrainStop> findByTrainAndStation(Train train, Station station);
}
