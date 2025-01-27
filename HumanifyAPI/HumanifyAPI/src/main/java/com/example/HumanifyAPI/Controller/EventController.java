package com.example.HumanifyAPI.Controller;

import com.example.HumanifyAPI.DTO.EventRequest;
import com.example.HumanifyAPI.DTO.EventResponse;
import com.example.HumanifyAPI.Enum.Category;
import com.example.HumanifyAPI.Model.Event;
import com.example.HumanifyAPI.Service.EventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api/v1/events")
public class EventController {
    @Autowired
    EventService eventService;

    @GetMapping("/{id}")
    public EventResponse getEvent(@PathVariable Integer id) throws IOException {
        return eventService.getEvent(id);
    }

    @GetMapping()
    public List<EventResponse> getEvents(@RequestParam(required = false) List<Category> category, @RequestParam(required = false) String place, @RequestParam(required = false) LocalDateTime until) throws IOException {
        if(category == null) {
            category = new ArrayList<>();
            for (Category i : Category.values()) {
                category.add(i);
            }
        }
        if(until == null){
            until = LocalDateTime.now().plusYears(2);
        }
        return eventService.getEvents(category,place,until);
    }

    @PostMapping()
    public void postEvent(@ModelAttribute EventRequest event) throws IOException {
        eventService.addEvent(event);
    }

    @DeleteMapping("/{id}")
    public void deleteEvent(@PathVariable Integer id){
         eventService.deleteEvent(id);
    }

}
