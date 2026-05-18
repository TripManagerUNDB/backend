package com.undb.TripManagerUNDB.cost.repository;

import com.undb.TripManagerUNDB.cost.entity.CostSummary;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface CostRepository extends MongoRepository<CostSummary, String> {
    List<CostSummary> findAllByTripId(String tripId);

    Optional<CostSummary> findFirstByTripId(String tripId);
}