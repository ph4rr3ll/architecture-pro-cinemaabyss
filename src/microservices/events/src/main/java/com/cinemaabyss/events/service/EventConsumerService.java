package com.cinemaabyss.events.service;

import com.cinemaabyss.events.model.Event;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Service
public class EventConsumerService {

    private static final Logger logger = LoggerFactory.getLogger(EventConsumerService.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    @KafkaListener(topics = "${kafka.topics.movie-events}", groupId = "${spring.kafka.consumer.group-id}")
    public void consumeMovieEvent(
            @Payload Event event,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset) {
        
        try {
            logger.info("=== MOVIE EVENT RECEIVED ===");
            logger.info("Partition: {}, Offset: {}", partition, offset);
            logger.info("Event ID: {}", event.getId());
            logger.info("Event Type: {}", event.getType());
            logger.info("Event Timestamp: {}", event.getTimestamp());
            logger.info("Event Payload: {}", objectMapper.writeValueAsString(event.getPayload()));
            logger.info("=============================");
        } catch (Exception e) {
            logger.error("Error processing movie event", e);
        }
    }

    @KafkaListener(topics = "${kafka.topics.user-events}", groupId = "${spring.kafka.consumer.group-id}")
    public void consumeUserEvent(
            @Payload Event event,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset) {
        
        try {
            logger.info("=== USER EVENT RECEIVED ===");
            logger.info("Partition: {}, Offset: {}", partition, offset);
            logger.info("Event ID: {}", event.getId());
            logger.info("Event Type: {}", event.getType());
            logger.info("Event Timestamp: {}", event.getTimestamp());
            logger.info("Event Payload: {}", objectMapper.writeValueAsString(event.getPayload()));
            logger.info("============================");
        } catch (Exception e) {
            logger.error("Error processing user event", e);
        }
    }

    @KafkaListener(topics = "${kafka.topics.payment-events}", groupId = "${spring.kafka.consumer.group-id}")
    public void consumePaymentEvent(
            @Payload Event event,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset) {
        
        try {
            logger.info("=== PAYMENT EVENT RECEIVED ===");
            logger.info("Partition: {}, Offset: {}", partition, offset);
            logger.info("Event ID: {}", event.getId());
            logger.info("Event Type: {}", event.getType());
            logger.info("Event Timestamp: {}", event.getTimestamp());
            logger.info("Event Payload: {}", objectMapper.writeValueAsString(event.getPayload()));
            logger.info("===============================");
        } catch (Exception e) {
            logger.error("Error processing payment event", e);
        }
    }
}

