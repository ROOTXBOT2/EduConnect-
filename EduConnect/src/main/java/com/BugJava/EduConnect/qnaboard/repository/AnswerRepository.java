package com.BugJava.EduConnect.qnaboard.repository;

import com.BugJava.EduConnect.qnaboard.entity.Answer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * @author rua
 */
public interface AnswerRepository extends JpaRepository<Answer, Long> {
    long countByQuestionIdAndIsDeletedFalse(Long questionId);
    List<Answer> findByQuestionIdAndIsDeletedFalse(Long questionId);
}
