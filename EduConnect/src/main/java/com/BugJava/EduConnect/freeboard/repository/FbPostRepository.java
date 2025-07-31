package com.BugJava.EduConnect.freeboard.repository;

import com.BugJava.EduConnect.freeboard.domain.FbPost;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FbPostRepository extends JpaRepository<FbPost, Long> {
    // 필요 시 검색 기능 추가
}