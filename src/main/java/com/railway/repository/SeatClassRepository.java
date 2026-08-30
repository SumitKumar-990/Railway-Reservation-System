package com.railway.repository;

import com.railway.model.SeatClass;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SeatClassRepository extends JpaRepository<SeatClass, Long> {
    Optional<SeatClass> findByCode(String code);
}
