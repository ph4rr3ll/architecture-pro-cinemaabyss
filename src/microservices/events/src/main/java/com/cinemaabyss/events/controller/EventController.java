package com.cinemaabyss.events.controller;

import com.cinemaabyss.events.model.*;
import com.cinemaabyss.events.service.EventProducerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/events")
public class EventController {

    private static final Logger logger = LoggerFactory.getLogger(EventController.class);

    private final EventProducerService eventProducerService;

    public EventController(EventProducerService eventProducerService) {
        this.eventProducerService = eventProducerService;
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Boolean>> health() {
        Map<String, Boolean> response = new HashMap<>();
        response.put("status", true);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/movie")
    public ResponseEntity<?> createMovieEvent(@RequestBody MovieEvent movieEvent) {
        try {
            logger.info("Received movie event request: movieId={}, title={}, action={}", 
                movieEvent.getMovieId(), movieEvent.getTitle(), movieEvent.getAction());
            
            EventResponse response = eventProducerService.publishMovieEvent(movieEvent);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            logger.error("Error creating movie event", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Internal Server Error: " + e.getMessage()));
        }
    }

    @PostMapping("/user")
    public ResponseEntity<?> createUserEvent(@RequestBody UserEvent userEvent) {
        try {
            logger.info("Received user event request: userId={}, action={}", 
                userEvent.getUserId(), userEvent.getAction());
            
            EventResponse response = eventProducerService.publishUserEvent(userEvent);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            logger.error("Error creating user event", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Internal Server Error: " + e.getMessage()));
        }
    }

    @PostMapping("/payment")
    public ResponseEntity<?> createPaymentEvent(@RequestBody PaymentEvent paymentEvent) {
        try {
            logger.info("Received payment event request: paymentId={}, userId={}, amount={}", 
                paymentEvent.getPaymentId(), paymentEvent.getUserId(), paymentEvent.getAmount());
            
            EventResponse response = eventProducerService.publishPaymentEvent(paymentEvent);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            logger.error("Error creating payment event", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Internal Server Error: " + e.getMessage()));
        }
    }
}

