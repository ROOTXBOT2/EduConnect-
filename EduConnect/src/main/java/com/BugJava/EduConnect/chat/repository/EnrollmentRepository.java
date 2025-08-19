package com.BugJava.EduConnect.chat.repository;

import com.BugJava.EduConnect.chat.domain.Enrollment;
import com.BugJava.EduConnect.chat.domain.Room;
import com.BugJava.EduConnect.auth.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {
    Optional<Enrollment> findByUserAndRoom(Users user, Room room);
    List<Enrollment> findByUser(Users user);
    boolean existsByUserAndRoom(Users user, Room room);
}
