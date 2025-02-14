package com.example.HumanifyAPI.Service;

import com.example.HumanifyAPI.DTO.DiscussionResponse;
import com.example.HumanifyAPI.DTO.EventRequest;
import com.example.HumanifyAPI.DTO.EventResponse;
import com.example.HumanifyAPI.DTO.UserResponse;
import com.example.HumanifyAPI.Enum.Category;
import com.example.HumanifyAPI.Model.*;
import com.example.HumanifyAPI.Repository.EventPictureRepository;
import com.example.HumanifyAPI.Repository.EventRepository;
import com.example.HumanifyAPI.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class EventService {
    @Value("${files.event-pictures}")
    String uploadDir;
    @Autowired
    EventRepository eventRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    EventPictureRepository eventPictureRepository;
    public EventResponse getEvent(Integer id) throws IOException {
        Optional<Event> eventoptional = eventRepository.findById(id);
        Event event = eventoptional.get();
        List<byte[]> pictures = new ArrayList<>();
        for( EventPicture i : event.getPictures()){
            pictures.add(Files.readAllBytes(Path.of(i.getPictureUrl())));
        }
        UserResponse userResponse = new UserResponse(event.getUser().getId(), event.getUser().getUsername(), event.getUser().getEmail(), event.getUser().getFirstName(), event.getUser().getLastName(), event.getUser().getBirthDate(),(event.getUser().getProfilePictureUrl().isEmpty())? null :Files.readAllBytes(Path.of(event.getUser().getProfilePictureUrl())));
        List<DiscussionResponse> discussionResponses = new ArrayList<>();
        for(Discussion d : event.getDiscussions()){
            UserResponse userResponseD = new UserResponse(d.getUser().getId(), d.getUser().getUsername(), d.getUser().getEmail(), d.getUser().getFirstName(), d.getUser().getLastName(), d.getUser().getBirthDate(),(d.getUser().getProfilePictureUrl().isEmpty())? null :Files.readAllBytes(Path.of(d.getUser().getProfilePictureUrl())));
            discussionResponses.add(new DiscussionResponse(d.getId(),d.getText(),d.getDateTime(),userResponseD));
        }
        return new EventResponse(event.getId(), event.getPlace(), event.getTitle(), event.getDescription(),event.getCategory(),event.getDateTime(),event.getLat(),event.getLon(),userResponse,event.getParticipants(),discussionResponses,pictures);
    }

    public List<EventResponse> getEvents(List<Category> category, String place, LocalDateTime until) throws IOException {
        List<Event> events;
        if(place == null || place.isEmpty()){
            events = eventRepository.findByCategoryInAndDateTimeBefore(category, until);
        }else {
            events = eventRepository.findByCategoryInAndPlaceContainingAndDateTimeBefore(category, place, until);
        }
        List<EventResponse> response = new ArrayList<>();
        for( Event i : events){
            List<byte[]> pictures = new ArrayList<>();
            for( EventPicture j : i.getPictures()){
                pictures.add(Files.readAllBytes(Path.of(j.getPictureUrl())));
            }
            UserResponse userResponse = new UserResponse(i.getUser().getId(), i.getUser().getUsername(), i.getUser().getEmail(), i.getUser().getFirstName(), i.getUser().getLastName(), i.getUser().getBirthDate(),(i.getUser().getProfilePictureUrl().isEmpty())? null :Files.readAllBytes(Path.of(i.getUser().getProfilePictureUrl())));
            List<DiscussionResponse> discussionResponses = new ArrayList<>();
            for(Discussion d : i.getDiscussions()){
                UserResponse userResponseD = new UserResponse(d.getUser().getId(), d.getUser().getUsername(), d.getUser().getEmail(), d.getUser().getFirstName(), d.getUser().getLastName(), d.getUser().getBirthDate(),(d.getUser().getProfilePictureUrl().isEmpty())? null :Files.readAllBytes(Path.of(d.getUser().getProfilePictureUrl())));
                discussionResponses.add(new DiscussionResponse(d.getId(),d.getText(),d.getDateTime(),userResponseD));
            }
            EventResponse curr = new EventResponse(i.getId(), i.getPlace(), i.getTitle(), i.getDescription(),i.getCategory(),i.getDateTime(),i.getLat(),i.getLon(),userResponse,i.getParticipants(),discussionResponses,pictures);
            response.add(curr);
        }
        return response;
    }

    public void addEvent(EventRequest request) throws IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        MyUserDetails myUserDetails = (MyUserDetails) authentication.getPrincipal();
        User user = userRepository.findUserByUsername(myUserDetails.getUsername());

        Event event = Event.builder()
                .user(user)
                .category(request.getCategory())
                .dateTime(request.getDateTime())
                .lat(request.getLat())
                .lon(request.getLon())
                .description(request.getDescription())
                .title(request.getTitle())
                .place(request.getPlace())
                .build();
        event.setUser(user);
        Event eventsaved = eventRepository.save(event);
        if(request.getPictures()!=null) {
            for (MultipartFile i : request.getPictures()) {
                String fileName = UUID.randomUUID() + "_" + i.getOriginalFilename();
                Path filePath = Paths.get(uploadDir, fileName);
                Files.createDirectories(filePath.getParent());
                Files.write(filePath, i.getBytes());
                EventPicture pom = EventPicture.builder()
                        .pictureUrl(String.valueOf(filePath))
                        .event(eventsaved)
                        .build();
                eventPictureRepository.save(pom);
            }
        }


    }

    public void deleteEvent(Integer id) {
        eventRepository.deleteById(id);
    }

    public List<Event> getParticipating(Integer id) {
        Optional<User> user = userRepository.findById(id);
        return eventRepository.findByParticipantsContaining(user.get());
    }

    public List<Event> getOrganizing(Integer id) {
        Optional<User> user = userRepository.findById(id);
        return eventRepository.findByUser(user.get());
    }

    public List<EventResponse> getEventsByLocation(List<Category> category, String place, LocalDateTime until, Integer range, Double lat, Double lon) throws IOException {
        List<Event> events;
        if(place == null || place.isEmpty()){
            events = eventRepository.findByCategoryInAndDateTimeBefore(category, until);
        }else {
            events = eventRepository.findByCategoryInAndPlaceContainingAndDateTimeBefore(category, place, until);
        }
        //filter out the events by range
        if (lat != null && lon != null && range != null) {
            events = filterEventsByRange(events, lat, lon, range);
        }

        List<EventResponse> response = new ArrayList<>();
        for( Event i : events){
            List<byte[]> pictures = new ArrayList<>();
            for( EventPicture j : i.getPictures()){
                pictures.add(Files.readAllBytes(Path.of(j.getPictureUrl())));
            }
            UserResponse userResponse = new UserResponse(i.getUser().getId(), i.getUser().getUsername(), i.getUser().getEmail(), i.getUser().getFirstName(), i.getUser().getLastName(), i.getUser().getBirthDate(),(i.getUser().getProfilePictureUrl().isEmpty())? null :Files.readAllBytes(Path.of(i.getUser().getProfilePictureUrl())));
            List<DiscussionResponse> discussionResponses = new ArrayList<>();
            for(Discussion d : i.getDiscussions()){
                UserResponse userResponseD = new UserResponse(d.getUser().getId(), d.getUser().getUsername(), d.getUser().getEmail(), d.getUser().getFirstName(), d.getUser().getLastName(), d.getUser().getBirthDate(),(d.getUser().getProfilePictureUrl().isEmpty())? null :Files.readAllBytes(Path.of(d.getUser().getProfilePictureUrl())));
                discussionResponses.add(new DiscussionResponse(d.getId(),d.getText(),d.getDateTime(),userResponseD));
            }
            EventResponse curr = new EventResponse(i.getId(), i.getPlace(), i.getTitle(), i.getDescription(),i.getCategory(),i.getDateTime(),i.getLat(),i.getLon(),userResponse,i.getParticipants(),discussionResponses,pictures);
            response.add(curr);
        }
        return response;
    }
    private List<Event> filterEventsByRange(List<Event> events, double userLat, double userLon, double rangeKm) {
        return events.stream()
                .filter(event -> haversine(userLat, userLon, event.getLat(), event.getLon()) <= rangeKm)
                .collect(Collectors.toList());
    }
    private double haversine(double lat1, double lon1, double lat2, double lon2) {
        final int EARTH_RADIUS = 6371; // Radius of the Earth in km
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS * c;
    }



}
