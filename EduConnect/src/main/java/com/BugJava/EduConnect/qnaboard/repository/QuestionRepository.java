package com.BugJava.EduConnect.qnaboard.repository;

import com.BugJava.EduConnect.auth.enums.Track;
import com.BugJava.EduConnect.qnaboard.entity.Question;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

/**
 * @author rua
 */
@Repository
public interface QuestionRepository extends JpaRepository<Question, Long>, JpaSpecificationExecutor<Question> {
    @EntityGraph(attributePaths = {"user"})
    Page<Question> findAll(Specification<Question> spec, Pageable pageable);
}
