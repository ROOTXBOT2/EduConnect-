package com.BugJava.EduConnect.freeboard.repository;

import com.BugJava.EduConnect.freeboard.domain.FbComment;
import com.BugJava.EduConnect.freeboard.domain.FbPost;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FbCommentRepository extends JpaRepository<FbComment, Long> {
    List<FbComment> findAllByPost(FbPost post);
}

