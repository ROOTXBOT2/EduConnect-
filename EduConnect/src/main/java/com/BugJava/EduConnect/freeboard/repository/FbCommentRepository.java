package com.BugJava.EduConnect.freeboard.repository;

import com.BugJava.EduConnect.freeboard.domain.FbComment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor; // JpaSpecificationExecutor 임포트

import java.util.Optional;

public interface FbCommentRepository extends JpaRepository<FbComment, Long>, JpaSpecificationExecutor<FbComment> {
    @EntityGraph(attributePaths = {"user", "post"})
    Optional<FbComment> findById(Long id);

    @EntityGraph(attributePaths = "user")
    Page<FbComment> findByPostId(Long postId, Pageable pageable);
}