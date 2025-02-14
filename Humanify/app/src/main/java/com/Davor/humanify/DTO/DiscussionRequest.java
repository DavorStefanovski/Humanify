package com.Davor.humanify.DTO;

import java.time.LocalDateTime;

public class DiscussionRequest {
    private String text;
    private LocalDateTime dateTime;
    private Integer eventId;

    public DiscussionRequest(String text, LocalDateTime dateTime, Integer eventId) {
        this.text = text;
        this.dateTime = dateTime;
        this.eventId = eventId;
    }
}
