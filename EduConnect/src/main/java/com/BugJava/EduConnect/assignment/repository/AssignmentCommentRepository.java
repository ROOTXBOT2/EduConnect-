package com.BugJava.EduConnect.assignment.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import com.BugJava.EduConnect.assignment.domain.AssignmentComment;


import java.util.Optional;

public interface AssignmentCommentRepository extends JpaRepository<AssignmentComment, Long> {

    @EntityGraph(attributePaths = {"user", "assignment"})
    Optional<AssignmentComment> findById(Long id);

}
