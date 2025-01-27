package com.example.HumanifyAPI.DTO;

import com.example.HumanifyAPI.Model.Event;
import com.example.HumanifyAPI.Model.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AddDiscussionRequest {
    private String text;
    private LocalDateTime dateTime;
    private Integer eventId;
}
