package com.BugJava.EduConnect.assignment.repository;

import com.BugJava.EduConnect.assignment.domain.Assignment;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AssignmentRepository extends JpaRepository<Assignment, Long> {

    @EntityGraph(attributePaths = {"user", "comments", "comments.user"})
    Optional<Assignment> findWithCommentsById(Long id);

    @EntityGraph(attributePaths = "user")
    List<Assignment> findAll();
}
