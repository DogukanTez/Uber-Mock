package com.dogukantez.matchingservice.service;

import com.dogukantez.matchingservice.client.LocationServiceClient;
import com.dogukantez.matchingservice.dto.NearByDriverResponse;
import com.dogukantez.matchingservice.event.RideMatchedEvent;
import com.dogukantez.matchingservice.event.RideRequestedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class MatchingService {
    private final LocationServiceClient locationServiceClient;
    private final KafkaTemplate<String, RideMatchedEvent> kafkaTemplate;

    public MatchingService(LocationServiceClient locationServiceClient, KafkaTemplate<String, RideMatchedEvent> kafkaTemplate) {
        this.locationServiceClient = locationServiceClient;
        this.kafkaTemplate = kafkaTemplate;
    }


    private static final String RIDE_MATCHED_TOPIC = "ride.matched";
    private static final double DEFAULT_SEARCH_RADIUS_KM = 5.0;

    public void matchDriverForRide(RideRequestedEvent event){

        List<NearByDriverResponse> nearByDrivers = locationServiceClient.getNearByDrivers(
                event.getPickupLatitude(),
                event.getPickupLongitude(),
                DEFAULT_SEARCH_RADIUS_KM
        );

        if(nearByDrivers.isEmpty()){
            log.warn("No drivers found near ride");
            return;
        }

        // Score each driver and pick the best one
        Optional<NearByDriverResponse> bestDriver = findBestDriver(nearByDrivers);

        if(bestDriver.isEmpty()){
            log.warn("could not find suitable driver for ride");
            return;
        }

        NearByDriverResponse assignedDriver = bestDriver.get();

        // Publish RideMatchedEvent to Kafka
        RideMatchedEvent matchedEvent = new RideMatchedEvent(
                event.getRideId(),
                event.getRiderId(),
                assignedDriver.getDriverId(),
                assignedDriver.getLatitude(),
                assignedDriver.getLongitude(),
                assignedDriver.getDistanceInKm()
        );

        kafkaTemplate.send(RIDE_MATCHED_TOPIC, event.getRideId(), matchedEvent);
        log.info("RideMatchedEvent published");
    }


    /**
     * Driver Scoring algorithm
     * Distance: 70%
     * Rating: 30%
     * Score = (1 / distance) * distanceWeight + rating * ratingWeight
     */

    private Optional<NearByDriverResponse> findBestDriver(
            List<NearByDriverResponse> drivers){

        double distanceWeight = 0.7;
        double ratingWeight = 0.3;

        return drivers.stream()
                .max(Comparator.comparingDouble(driver -> {
                    // closer = higher score
                    // Add 0.1 to avoid division by zero
                    double distanceScore = 1.0/(driver.getDistanceInKm() + 0.1);

                    // Simulated rating between 4.0 and 5.0
                    // In production: fetch from Driver Service

                    double simulatedRating = 4.0 + Math.random();

                    //Final weighted score
                    return (distanceScore * distanceWeight)
                            + (simulatedRating * ratingWeight);
                }));
    }



}
