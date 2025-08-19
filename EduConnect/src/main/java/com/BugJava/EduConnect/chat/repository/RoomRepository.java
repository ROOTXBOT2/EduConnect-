package com.BugJava.EduConnect.chat.repository;

import com.BugJava.EduConnect.chat.domain.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import com.BugJava.EduConnect.auth.entity.Users;

import java.util.List;
import java.util.Optional;

public interface RoomRepository extends JpaRepository<Room, Long> {
    Optional<Room> findByCode(String code);
    List<Room> findByInstructor(Users instructor);
}
