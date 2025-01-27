package com.example.HumanifyAPI.Controller;

import com.example.HumanifyAPI.DTO.AddDiscussionRequest;
import com.example.HumanifyAPI.Model.Discussion;
import com.example.HumanifyAPI.Model.Event;
import com.example.HumanifyAPI.Repository.DiscussionRepository;
import com.example.HumanifyAPI.Service.DiscussionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/discussions")
public class DiscussionController {
    @Autowired
    DiscussionService discussionService;

    @PostMapping()
    public Discussion addDiscussion(@RequestBody AddDiscussionRequest addDiscussionRequest){
        return discussionService.addDiscussion(addDiscussionRequest);
    }
}
