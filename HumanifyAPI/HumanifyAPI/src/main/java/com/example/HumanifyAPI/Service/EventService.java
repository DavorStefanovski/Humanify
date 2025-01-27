package com.example.HumanifyAPI.Service;

import com.example.HumanifyAPI.DTO.EventRequest;
import com.example.HumanifyAPI.DTO.EventResponse;
import com.example.HumanifyAPI.Enum.Category;
import com.example.HumanifyAPI.Model.Event;
import com.example.HumanifyAPI.Model.EventPicture;
import com.example.HumanifyAPI.Model.MyUserDetails;
import com.example.HumanifyAPI.Model.User;
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
        return new EventResponse(event.getId(), event.getPlace(), event.getTitle(), event.getDescription(),event.getCategory(),event.getDateTime(),event.getLat(),event.getLon(),event.getUser(),event.getParticipants(),event.getDiscussions(),pictures);
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
            EventResponse curr = new EventResponse(i.getId(), i.getPlace(), i.getTitle(), i.getDescription(),i.getCategory(),i.getDateTime(),i.getLat(),i.getLon(),i.getUser(),i.getParticipants(),i.getDiscussions(),pictures);
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

        for(MultipartFile i : request.getPictures()) {
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
}
