package com.cinemaabyss.events.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentEvent {
    @JsonProperty("payment_id")
    private Integer paymentId;

    @JsonProperty("user_id")
    private Integer userId;

    private Double amount;

    private String status;

    private String timestamp;

    @JsonProperty("method_type")
    private String methodType;
}

