package com.BugJava.EduConnect.qnaboard.service;

import com.BugJava.EduConnect.auth.entity.Users;
import com.BugJava.EduConnect.auth.exception.UserNotFoundException;
import com.BugJava.EduConnect.auth.repository.UserRepository;
import com.BugJava.EduConnect.qnaboard.dto.*;
import com.BugJava.EduConnect.qnaboard.entity.Question;
import com.BugJava.EduConnect.qnaboard.exception.AccessDeniedException;
import com.BugJava.EduConnect.qnaboard.exception.QuestionNotFoundException;
import com.BugJava.EduConnect.qnaboard.repository.AnswerRepository;
import com.BugJava.EduConnect.qnaboard.repository.CommentRepository;
import com.BugJava.EduConnect.qnaboard.repository.QuestionRepository;
import com.BugJava.EduConnect.qnaboard.repository.QuestionSpecs;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

/**
 * @author rua
 */
@Service
@RequiredArgsConstructor
public class QuestionService {
    final private QuestionRepository questionRepository;
    final private AnswerRepository answerRepository;
    final private CommentRepository commentRepository;
    final private UserRepository userRepository;

    @Transactional(readOnly = true)
    public Page<QuestionAllResponse> getAllQuestions(Pageable pageable) {
        // 전체 목록도 동일 빌더 사용: req == null이면 필터 생략
        return questionRepository.findAll(QuestionSpecs.buildSearch(null), pageable)
                .map(QuestionAllResponse::from);
    }

    @Transactional(readOnly=true)
    public Page<QuestionAllResponse> getSearchQuestions(QuestionSearchRequest req, Pageable pageable) {
        return questionRepository.findAll(QuestionSpecs.buildSearch(req), pageable)
                .map(QuestionAllResponse::from);
    }

    @Transactional(readOnly = true)
    public QuestionResponse getQuestionDetail(Long id) {
        Question q = questionRepository.findById(id)
                .filter(qq -> !qq.isDeleted())
                .orElseThrow(() -> new QuestionNotFoundException(id.toString()));

        long aCount = answerRepository.countByQuestionIdAndIsDeletedFalse(id);
        long cCount = commentRepository.countByQuestionId(id);

        return QuestionResponse.builder()
                .id(q.getId())
                .title(q.getTitle())
                .content(q.getContent())
                .writerName(q.getUser().getName())
                .startTrack(q.getUser().getTrack().name())
                .endTrack(q.getTrack().name())
                .createdAt(q.getCreatedAt())
                .answerCount(aCount)
                .commentCount(cCount)
                .build();
    }

    @Transactional
    public void createQuestion(Long userId, QuestionCreateRequest req) {
        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("로그인 사용자 없음"));

        // 권한: 학생/강사만 (추가 도메인 검증 필요시 여기서)
        if (!(user.hasRole("STUDENT") || user.hasRole("INSTRUCTOR")))
            throw new AccessDeniedException("권한 없음");

        Question q = Question.builder()
                .title(req.getTitle())
                .content(req.getContent())
                .track(req.getTrack())
                .user(user)
                .isDeleted(false)
                .build();

        questionRepository.save(q);
    }

    @Transactional
    public void updateQuestion(Long userId, Long questionId, QuestionUpdateRequest req) {
        Question question = questionRepository.findById(questionId)
                .filter(q -> !q.isDeleted())
                .orElseThrow(() -> new QuestionNotFoundException(questionId.toString()));

        if (!question.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("본인의 질문만 수정할 수 있습니다.");
        }

        question.change(req.getTitle(), req.getContent(), req.getTrack());
        questionRepository.save(question);
    }

    @Transactional
    public void deleteQuestion(Long userId, Long questionId) {
        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("로그인 사용자 없음"));

        Question question = questionRepository.findById(questionId)
                .filter(q -> !q.isDeleted())
                .orElseThrow(() -> new QuestionNotFoundException(questionId.toString()));

        // 작성자 본인만 삭제 가능
        if (!question.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("본인의 질문만 삭제할 수 있습니다.");
        }

        question.softDelete();
        questionRepository.save(question);
    }
}
