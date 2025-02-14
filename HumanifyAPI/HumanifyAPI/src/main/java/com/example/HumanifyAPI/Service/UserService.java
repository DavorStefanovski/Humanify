package com.example.HumanifyAPI.Service;

import com.example.HumanifyAPI.DTO.*;
import com.example.HumanifyAPI.Model.*;
import com.example.HumanifyAPI.Repository.EventRepository;
import com.example.HumanifyAPI.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
@Service
public class UserService {
    @Value("${files.profile-pictures}")
    String uploadDir;
    @Autowired
    JwtService jwtService;
    @Autowired
    UserRepository userRepository;
    @Autowired
    EventRepository eventRepository;
    @Autowired
    AuthenticationManager authenticationManager;

    private BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);

    public UserResponse register(UserRequest request) throws IOException { //custom dto?
        Path filePath = null;
        if(request.getProfilePicture()!=null) {
            String fileName = UUID.randomUUID() + "_" + request.getProfilePicture().getOriginalFilename();
            filePath = Paths.get(uploadDir, fileName);
            Files.createDirectories(filePath.getParent());
            Files.write(filePath, request.getProfilePicture().getBytes());
        }
        User user = User.builder()
                .email(request.getEmail())
                .username(request.getUsername())
                .birthDate(request.getBirthDate())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .password(encoder.encode(request.getPassword()))
                .profilePictureUrl((request.getProfilePicture()==null)?"":filePath.toString())
                .build();
        userRepository.save(user);
        return new UserResponse(user.getId(), user.getUsername(), user.getEmail(), user.getFirstName(), user.getLastName(), user.getBirthDate(),(user.getProfilePictureUrl().isEmpty())? null :Files.readAllBytes(Path.of(user.getProfilePictureUrl())));
    }

    public String verify(LoginData loginData) {
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginData.getUsername(),loginData.getPassword()));
        if(authentication.isAuthenticated()){
            return jwtService.generateToken(loginData.getUsername());
        }
        else{
            return "login failed";
        }
    }

    public UserResponse getUser(Integer id) throws IOException {
        Optional<User> userOptional = userRepository.findById(id);
        User user = userOptional.get();
        return new UserResponse(user.getId(), user.getUsername(), user.getEmail(), user.getFirstName(), user.getLastName(), user.getBirthDate(),(user.getProfilePictureUrl().isEmpty())? null :Files.readAllBytes(Path.of(user.getProfilePictureUrl())));
    }
    public UserResponse getCurrentUser() throws IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        MyUserDetails myUserDetails = (MyUserDetails) authentication.getPrincipal();
        User user = userRepository.findUserByUsername(myUserDetails.getUsername());
        return new UserResponse(user.getId(), user.getUsername(), user.getEmail(), user.getFirstName(), user.getLastName(), user.getBirthDate(),(user.getProfilePictureUrl().isEmpty())? null :Files.readAllBytes(Path.of(user.getProfilePictureUrl())));
    }


    public void participate(Integer id) {
        Optional<Event> event = eventRepository.findById(id);
        if(event.isPresent()){
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            MyUserDetails myUserDetails = (MyUserDetails) authentication.getPrincipal();
            User user = userRepository.findUserByUsername(myUserDetails.getUsername());
            user.getParticipatingEvents().add(event.get());
            userRepository.save(user);
        }else{
            throw new RuntimeException("Event not found");
        }
    }
    public void uploadProfilePicture(MultipartFile file) throws IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        MyUserDetails myUserDetails = (MyUserDetails) authentication.getPrincipal();
        User user = userRepository.findUserByUsername(myUserDetails.getUsername());
        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
        Path filePath = Paths.get(uploadDir, fileName);

        Files.createDirectories(filePath.getParent());
        Files.write(filePath, file.getBytes());

        user.setProfilePictureUrl(filePath.toString());
        userRepository.save(user);
    }

    public UserWithEventsResponse getCurrentUserWithEvents() throws IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        MyUserDetails myUserDetails = (MyUserDetails) authentication.getPrincipal();
        User user = userRepository.findUserByUsername(myUserDetails.getUsername());
        List<Event> eventList = user.getParticipatingEvents();
        List<EventResponse> response = new ArrayList<>();
        for( Event i : eventList){
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
        return new UserWithEventsResponse(user.getId(), user.getUsername(),user.getEmail(),user.getFirstName(), user.getLastName(), user.getBirthDate(),(user.getProfilePictureUrl().isEmpty())? null:Files.readAllBytes(Path.of(user.getProfilePictureUrl())),response);
    }
}
