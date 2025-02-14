package com.Davor.humanify.Model;


import com.Davor.humanify.Enum.Category;

import java.time.LocalDateTime;
import java.util.List;

public class Event {
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
    private List<EventPicture> pictures;
}
