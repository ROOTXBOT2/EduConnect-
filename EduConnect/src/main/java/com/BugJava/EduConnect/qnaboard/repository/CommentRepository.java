package com.BugJava.EduConnect.qnaboard.repository;

import com.BugJava.EduConnect.qnaboard.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * @author rua
 */
public interface CommentRepository extends JpaRepository<Comment, Long> {
    @Query("select count(c) from Comment c where c.answer.question.id = :questionId")
    long countByQuestionId(@Param("questionId") Long questionId);

    List<Comment> findByAnswerIdAndIsDeletedFalse(Long answerId);
}