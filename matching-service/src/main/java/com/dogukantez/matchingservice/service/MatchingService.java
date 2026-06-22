package com.dogukantez.matchingservice.service;

import com.dogukantez.matchingservice.client.LocationServiceClient;
import com.dogukantez.matchingservice.event.RideMatchedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class MatchingService {
    private final LocationServiceClient locationServiceClient;
    private final KafkaTemplate<String, RideMatchedEvent> kafkaTemplate;

    public MatchingService(LocationServiceClient locationServiceClient, KafkaTemplate<String, RideMatchedEvent> kafkaTemplate) {
        this.locationServiceClient = locationServiceClient;
        this.kafkaTemplate = kafkaTemplate;
    }
}
