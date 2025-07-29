package com.BugJava.EduConnect.post.repository;

import com.BugJava.EduConnect.post.domain.Post;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, Long> {
    // 필요 시 검색 기능 추가
}