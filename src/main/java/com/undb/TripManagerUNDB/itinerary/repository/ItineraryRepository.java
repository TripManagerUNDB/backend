package com.undb.TripManagerUNDB.itinerary.repository;

import com.undb.TripManagerUNDB.itinerary.entity.ItineraryDay;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ItineraryRepository extends MongoRepository<ItineraryDay, String> {
    List<ItineraryDay> findByTripIdOrderByDayNumber(String tripId);
    void deleteByTripId(String tripId);
}
