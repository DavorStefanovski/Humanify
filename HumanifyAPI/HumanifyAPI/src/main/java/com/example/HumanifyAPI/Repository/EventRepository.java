package com.example.HumanifyAPI.Repository;

import com.example.HumanifyAPI.Enum.Category;
import com.example.HumanifyAPI.Model.Event;
import com.example.HumanifyAPI.Model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

public interface EventRepository extends JpaRepository<Event,Integer> {
    List<Event> findByCategoryInAndPlaceContainingAndDateTimeBefore(List<Category> category, String place, LocalDateTime until);
    List<Event> findByCategoryInAndDateTimeBefore(List<Category> category, LocalDateTime until);
    List<Event> findByUser(User id);
    List<Event> findByParticipantsContaining(User id);
}
