package com.BugJava.EduConnect.freeboard.repository;

import com.BugJava.EduConnect.freeboard.domain.FbComment;
import com.BugJava.EduConnect.freeboard.domain.FbPost;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import java.util.Optional;

public interface FbCommentRepository extends JpaRepository<FbComment, Long> {
    @EntityGraph(attributePaths = {"user", "post"})
    Optional<FbComment> findById(Long id);
}

