package com.BugJava.EduConnect.freeboard.repository;

import com.BugJava.EduConnect.freeboard.domain.FbPost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor; // JpaSpecificationExecutor 임포트

import java.util.List;
import java.util.Optional;

public interface FbPostRepository extends JpaRepository<FbPost, Long>, JpaSpecificationExecutor<FbPost> {
    // 필요 시 검색 기능 추가

    @EntityGraph(attributePaths = {"user", "comments", "comments.user"}) // comments.user 추가
    Optional<FbPost> findWithCommentsById(Long id);

    @EntityGraph(attributePaths = "user")
    List<FbPost> findAll();
}