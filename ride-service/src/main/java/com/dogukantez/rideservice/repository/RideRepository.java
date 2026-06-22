package com.dogukantez.rideservice.repository;

import com.dogukantez.rideservice.entity.Ride;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RideRepository extends JpaRepository<Ride,String> {
    List<Ride> findByRiderIdOrderByCreatedAtDesc(String riderId);
}
