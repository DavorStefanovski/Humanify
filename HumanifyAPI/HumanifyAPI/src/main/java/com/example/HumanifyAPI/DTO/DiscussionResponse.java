package com.example.HumanifyAPI.DTO;

import com.example.HumanifyAPI.Model.Event;
import com.example.HumanifyAPI.Model.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
public class DiscussionResponse {
    private Integer id;
    private String text;
    private LocalDateTime dateTime;
    private UserResponse user;
}
