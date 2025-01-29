package com.example.HumanifyAPI.Endpoint;

import com.example.HumanifyAPI.DTO.EventRequest;
import com.example.HumanifyAPI.DTO.EventResponse;
import com.example.HumanifyAPI.DTO.LoginData;
import com.example.HumanifyAPI.DTO.UserResponse;
import com.example.HumanifyAPI.Enum.Category;
import com.example.HumanifyAPI.Service.EventService;
import com.example.HumanifyAPI.Service.UserService;
import com.example.soap.GetEventResponse;
import com.example.soap.GetUserResponse;
import com.example.soap.LoginResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.GregorianCalendar;

@Endpoint
public class HumanifyEndpoint {

    private static final String NAMESPACE_URI = "http://example.com/soap";

    @Autowired
    private UserService userService;

    @Autowired
    private EventService eventService;

    // Register user
    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "registerUserRequest")
    @ResponsePayload
    public com.example.soap.RegisterUserResponse register(@RequestPayload com.example.soap.RegisterUserRequest userRequest) throws IOException, DatatypeConfigurationException {
        com.example.HumanifyAPI.DTO.UserRequest restUserRequest = new com.example.HumanifyAPI.DTO.UserRequest();
        restUserRequest.setUsername(userRequest.getUsername());
        restUserRequest.setPassword(userRequest.getPassword());
        restUserRequest.setEmail(userRequest.getEmail());
        restUserRequest.setFirstName(userRequest.getFirstName());
        restUserRequest.setLastName(userRequest.getLastName());
        restUserRequest.setBirthDate(userRequest.getBirthDate().toGregorianCalendar().toZonedDateTime().toLocalDate());

        com.example.HumanifyAPI.DTO.UserResponse restUserResponse = userService.register(restUserRequest);

        com.example.soap.RegisterUserResponse soapUserResponse = new com.example.soap.RegisterUserResponse();
        soapUserResponse.setId(restUserResponse.getId());
        soapUserResponse.setUsername(restUserResponse.getUsername());
        soapUserResponse.setEmail(restUserResponse.getEmail());
        soapUserResponse.setFirstName(restUserResponse.getFirstName());
        soapUserResponse.setLastName(restUserResponse.getLastName());
        soapUserResponse.setBirthDate(DatatypeFactory.newInstance().newXMLGregorianCalendar(restUserResponse.getBirthDate().toString()));
        soapUserResponse.setProfilePicture(restUserResponse.getProfilePicture());

        return soapUserResponse;
    }

    // Login user
    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "loginRequest")
    @ResponsePayload
    public com.example.soap.LoginResponse login(@RequestPayload com.example.soap.LoginRequest loginRequest) {
        LoginData loginData = new LoginData();
        loginData.setUsername(loginRequest.getUsername());
        loginData.setPassword(loginRequest.getPassword());
        String token = userService.verify(loginData);
        com.example.soap.LoginResponse response = new LoginResponse();
        response.setToken(token);
        return  response;
    }

    // Get user by ID
    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "getUserRequest")
    @ResponsePayload
    public com.example.soap.GetUserResponse getUser(@RequestPayload com.example.soap.GetUserRequest getUserRequest) throws IOException, DatatypeConfigurationException {
        Integer id = getUserRequest.getUserId();
        UserResponse restUserResponse = userService.getUser(id);
        com.example.soap.GetUserResponse soapUserResponse = new GetUserResponse();
        soapUserResponse.setId(restUserResponse.getId());
        soapUserResponse.setUsername(restUserResponse.getUsername());
        soapUserResponse.setEmail(restUserResponse.getEmail());
        soapUserResponse.setFirstName(restUserResponse.getFirstName());
        soapUserResponse.setLastName(restUserResponse.getLastName());
        soapUserResponse.setBirthDate(DatatypeFactory.newInstance().newXMLGregorianCalendar(restUserResponse.getBirthDate().toString()));
        soapUserResponse.setProfilePicture(restUserResponse.getProfilePicture());
        return  soapUserResponse;
    }

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "getEventRequest")
    @ResponsePayload
    public com.example.soap.GetEventResponse getEvent(@RequestPayload com.example.soap.GetEventRequest getEventRequest) throws IOException, DatatypeConfigurationException {
        Integer id = getEventRequest.getId();
        EventResponse restEventResponse = eventService.getEvent(id);
        com.example.soap.GetEventResponse soapEventResponse = new GetEventResponse();
        soapEventResponse.setId(restEventResponse.getId());
        soapEventResponse.setCategory(restEventResponse.getCategory().name());
        soapEventResponse.setTitle(restEventResponse.getTitle());
        soapEventResponse.setLat(restEventResponse.getLat());
        soapEventResponse.setLon(restEventResponse.getLon());
        LocalDateTime localDateTime = restEventResponse.getDateTime();

        // Add a timezone (e.g., UTC) to the LocalDateTime
        ZonedDateTime zonedDateTime = localDateTime.atZone(ZoneId.of("UTC"));

        // Convert ZonedDateTime to GregorianCalendar
        GregorianCalendar gregorianCalendar = GregorianCalendar.from(zonedDateTime);

        // Create XMLGregorianCalendar using DatatypeFactory
        XMLGregorianCalendar xmlGregorianCalendar = DatatypeFactory.newInstance().newXMLGregorianCalendar(gregorianCalendar);

        // Set the XMLGregorianCalendar in soapEventResponse
        soapEventResponse.setDateTime(xmlGregorianCalendar);
        soapEventResponse.setPlace(restEventResponse.getPlace());
        soapEventResponse.setDescription(restEventResponse.getDescription());

        return  soapEventResponse;
    }

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "postEventRequest")
    @ResponsePayload
    public void postEvent(@RequestPayload com.example.soap.PostEventRequest postEventRequest) throws IOException, DatatypeConfigurationException {
        EventRequest restEventRequest = new EventRequest();
        restEventRequest.setTitle(postEventRequest.getTitle());
        restEventRequest.setDescription(postEventRequest.getDescription());
        restEventRequest.setCategory(Category.valueOf(postEventRequest.getCategory()));
        restEventRequest.setLat(postEventRequest.getLat());
        restEventRequest.setLon(postEventRequest.getLon());
        restEventRequest.setDateTime(postEventRequest.getDateTime().toGregorianCalendar().toZonedDateTime().toLocalDateTime());
        restEventRequest.setPlace(postEventRequest.getPlace());

        eventService.addEvent(restEventRequest);
    }


}
