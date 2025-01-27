package com.example.HumanifyAPI.DTO;

import com.example.HumanifyAPI.Enum.Category;
import com.example.HumanifyAPI.Model.Discussion;
import com.example.HumanifyAPI.Model.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
public class EventResponse {
    private Integer id;
    private String place;
    private String title;
    private String description;
    private Category category;
    private LocalDateTime dateTime;
    private Double lat;
    private Double lon;
    private User user;
    private List<User> participants;
    private List<Discussion> discussions;
    private List<byte[]> pictures;
}
