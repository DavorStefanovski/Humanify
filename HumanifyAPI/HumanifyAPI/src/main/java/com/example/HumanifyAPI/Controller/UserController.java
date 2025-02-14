package com.example.HumanifyAPI.Controller;

import com.example.HumanifyAPI.DTO.LoginData;
import com.example.HumanifyAPI.DTO.UserRequest;
import com.example.HumanifyAPI.DTO.UserResponse;
import com.example.HumanifyAPI.DTO.UserWithEventsResponse;
import com.example.HumanifyAPI.Model.Event;
import com.example.HumanifyAPI.Service.EventService;
import com.example.HumanifyAPI.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {
    @Autowired
    UserService userService;
    @Autowired
    EventService eventService;
    @PostMapping("/register")
    public UserResponse register(@ModelAttribute UserRequest user) throws IOException {
        return userService.register(user);
    }

    @PostMapping("/login")
    public String login(@RequestBody LoginData loginData) {
        return userService.verify(loginData);
    }

    @PostMapping("/changepic")
    public void changepic(@RequestParam MultipartFile file) throws IOException {
         userService.uploadProfilePicture(file);
    }

    @GetMapping("/{id}")
    public UserResponse getUser(@PathVariable Integer id) throws IOException {
        return userService.getUser(id);
    }


    @GetMapping("/currentuser")
    public UserResponse getCurrentUser() throws IOException {
        return userService.getCurrentUser();
    }

    @GetMapping("/currentuserevents")
    public UserWithEventsResponse getCurrentUserWithEvents() throws IOException {
        return userService.getCurrentUserWithEvents();
    }

    @PostMapping("/participate/{id}")
    public void participate(@PathVariable Integer id) {
         userService.participate(id);
    }

    @GetMapping("/participating/{id}")
    public List<Event> getParticipating(@PathVariable Integer id){
        return eventService.getParticipating(id);
    }

    @GetMapping("/organizing/{id}")
    public List<Event> getOrganizing(@PathVariable Integer id){
        return eventService.getOrganizing(id);
    }


}
