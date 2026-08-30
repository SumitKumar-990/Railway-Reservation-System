package com.railway.repository;

import com.railway.model.Station;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StationRepository extends JpaRepository<Station, Long> {
    Optional<Station> findByCode(String code);
    Optional<Station> findByCodeIgnoreCase(String code);
    Optional<Station> findByNameIgnoreCase(String name);
    List<Station> findAllByOrderByNameAsc();

    @Query("SELECT s FROM Station s WHERE UPPER(s.code) LIKE UPPER(CONCAT('%', :query, '%')) OR UPPER(s.name) LIKE UPPER(CONCAT('%', :query, '%')) OR UPPER(s.city) LIKE UPPER(CONCAT('%', :query, '%')) ORDER BY s.name ASC")
    List<Station> searchStations(@Param("query") String query);
}
