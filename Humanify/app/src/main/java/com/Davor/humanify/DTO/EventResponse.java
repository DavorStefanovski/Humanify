package com.Davor.humanify.DTO;

import com.Davor.humanify.Enum.Category;
import com.Davor.humanify.Model.Discussion;
import com.Davor.humanify.Model.User;

import java.time.LocalDateTime;
import java.util.List;

public class EventResponse {
    private Integer id;
    private String place;
    private String title;
    private String description;
    private Category category;
    private LocalDateTime dateTime;
    private Double lat;
    private Double lon;
    private UserResponse user;
    private List<User> participants;
    private List<DiscussionResponse> discussions;
    private List<byte[]> pictures;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getPlace() {
        return place;
    }

    public void setPlace(String place) {
        this.place = place;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }

    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public Double getLon() {
        return lon;
    }

    public void setLon(Double lon) {
        this.lon = lon;
    }

    public UserResponse getUser() {
        return user;
    }

    public void setUser(UserResponse user) {
        this.user = user;
    }

    public List<DiscussionResponse> getDiscussions() {
        return discussions;
    }

    public void setDiscussions(List<DiscussionResponse> discussions) {
        this.discussions = discussions;
    }

    public List<User> getParticipants() {
        return participants;
    }

    public void setParticipants(List<User> participants) {
        this.participants = participants;
    }



    public List<byte[]> getPictures() {
        return pictures;
    }

    public void setPictures(List<byte[]> pictures) {
        this.pictures = pictures;
    }
}
