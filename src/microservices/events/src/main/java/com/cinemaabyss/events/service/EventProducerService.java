package com.cinemaabyss.events.service;

import com.cinemaabyss.events.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
public class EventProducerService {

    private static final Logger logger = LoggerFactory.getLogger(EventProducerService.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${kafka.topics.movie-events}")
    private String movieEventsTopic;

    @Value("${kafka.topics.user-events}")
    private String userEventsTopic;

    @Value("${kafka.topics.payment-events}")
    private String paymentEventsTopic;

    public EventProducerService(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public EventResponse publishMovieEvent(MovieEvent movieEvent) {
        Event event = createEvent("movie", movieEvent);
        CompletableFuture<SendResult<String, Object>> future = 
            kafkaTemplate.send(movieEventsTopic, event.getId(), event);
        
        return handleSendResult(future, event);
    }

    public EventResponse publishUserEvent(UserEvent userEvent) {
        Event event = createEvent("user", userEvent);
        CompletableFuture<SendResult<String, Object>> future = 
            kafkaTemplate.send(userEventsTopic, event.getId(), event);
        
        return handleSendResult(future, event);
    }

    public EventResponse publishPaymentEvent(PaymentEvent paymentEvent) {
        Event event = createEvent("payment", paymentEvent);
        CompletableFuture<SendResult<String, Object>> future = 
            kafkaTemplate.send(paymentEventsTopic, event.getId(), event);
        
        return handleSendResult(future, event);
    }

    private Event createEvent(String type, Object payload) {
        String eventId = UUID.randomUUID().toString();
        String timestamp = Instant.now().toString();
        return new Event(eventId, type, timestamp, payload);
    }

    private EventResponse handleSendResult(CompletableFuture<SendResult<String, Object>> future, Event event) {
        try {
            SendResult<String, Object> result = future.get();
            int partition = result.getRecordMetadata().partition();
            long offset = result.getRecordMetadata().offset();
            
            logger.info("Event published successfully: type={}, id={}, partition={}, offset={}", 
                event.getType(), event.getId(), partition, offset);
            
            return new EventResponse("success", partition, offset, event);
        } catch (Exception e) {
            logger.error("Failed to publish event: type={}, id={}", event.getType(), event.getId(), e);
            throw new RuntimeException("Failed to publish event: " + e.getMessage(), e);
        }
    }
}

