package com.undb.TripManagerUNDB.cost.repository;

import com.undb.TripManagerUNDB.cost.entity.CostSummary;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface CostRepository extends MongoRepository<CostSummary, String> {
    Optional<CostSummary> findByTripId(String tripId);
}
