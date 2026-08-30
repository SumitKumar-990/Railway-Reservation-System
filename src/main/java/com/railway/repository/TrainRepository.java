package com.railway.repository;

import com.railway.model.Train;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TrainRepository extends JpaRepository<Train, Long> {
    Optional<Train> findByTrainNumber(String trainNumber);

    @Query("SELECT DISTINCT t FROM Train t JOIN t.stops s1 JOIN t.stops s2 WHERE s1.station.code = :fromCode AND s2.station.code = :toCode AND s1.sequenceNumber < s2.sequenceNumber")
    List<Train> findTrainsBetweenStations(@Param("fromCode") String fromCode, @Param("toCode") String toCode);
}
