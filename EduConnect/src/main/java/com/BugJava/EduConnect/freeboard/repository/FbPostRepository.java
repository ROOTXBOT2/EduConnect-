package com.BugJava.EduConnect.freeboard.repository;

import com.BugJava.EduConnect.freeboard.domain.FbPost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;

import java.util.List;
import java.util.Optional;

public interface FbPostRepository extends JpaRepository<FbPost, Long> {
    // 필요 시 검색 기능 추가

    @EntityGraph(attributePaths = {"comments", "user"})
    Optional<FbPost> findWithCommentsById(Long id);

    @EntityGraph(attributePaths = "user")
    List<FbPost> findAll();
}