package com.example.HumanifyAPI.Service;

import com.example.HumanifyAPI.DTO.AddDiscussionRequest;
import com.example.HumanifyAPI.Model.Discussion;
import com.example.HumanifyAPI.Model.Event;
import com.example.HumanifyAPI.Model.MyUserDetails;
import com.example.HumanifyAPI.Model.User;
import com.example.HumanifyAPI.Repository.DiscussionRepository;
import com.example.HumanifyAPI.Repository.EventRepository;
import com.example.HumanifyAPI.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class DiscussionService {
    @Autowired
    DiscussionRepository discussionRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    EventRepository eventRepository;
    public Discussion addDiscussion(AddDiscussionRequest addDiscussionRequest) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        MyUserDetails myUserDetails = (MyUserDetails) authentication.getPrincipal();
        User user = userRepository.findUserByUsername(myUserDetails.getUsername());
        Optional<Event> event = eventRepository.findById(addDiscussionRequest.getEventId());
        Discussion discussion = Discussion.builder()
                        .text(addDiscussionRequest.getText())
                                .dateTime(addDiscussionRequest.getDateTime())
                                        .event(event.get())
                                                .user(user)
                                                        .build();
        discussionRepository.save(discussion);
        return discussion;
    }
}
