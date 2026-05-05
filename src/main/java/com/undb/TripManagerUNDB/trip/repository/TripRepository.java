package com.undb.TripManagerUNDB.trip.repository;

import com.undb.TripManagerUNDB.trip.entity.Trip;
import com.undb.TripManagerUNDB.trip.enums.TripStatus;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface TripRepository extends MongoRepository<Trip, String> {
    List<Trip> findByUserIdOrderByCreatedAtDesc(String userId);
    List<Trip> findByUserIdAndStatusOrderByCreatedAtDesc(String userId, TripStatus status);
    long countByUserId(String userId);
}
