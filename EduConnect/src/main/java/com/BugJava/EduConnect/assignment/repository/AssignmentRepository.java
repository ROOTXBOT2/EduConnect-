package com.BugJava.EduConnect.assignment.repository;

import com.BugJava.EduConnect.assignment.domain.Assignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

public interface AssignmentRepository extends JpaRepository<Assignment, Long> {
}
