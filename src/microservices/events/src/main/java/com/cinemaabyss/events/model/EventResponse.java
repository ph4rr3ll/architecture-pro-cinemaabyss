package com.cinemaabyss.events.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventResponse {
    private String status;
    private Integer partition;
    private Long offset;
    private Event event;
}

