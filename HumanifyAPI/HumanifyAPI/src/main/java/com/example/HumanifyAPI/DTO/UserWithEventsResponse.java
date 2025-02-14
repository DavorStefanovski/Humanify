package com.example.HumanifyAPI.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
public class UserWithEventsResponse {
    private Integer id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private LocalDate birthDate;
    private byte[] profilePicture;
    private List<EventResponse> eventResponseList;
}
