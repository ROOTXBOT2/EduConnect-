package com.BugJava.EduConnect.qnaboard.service;

import com.BugJava.EduConnect.auth.entity.Users;
import com.BugJava.EduConnect.auth.exception.UserNotFoundException;
import com.BugJava.EduConnect.auth.repository.UserRepository;
import com.BugJava.EduConnect.qnaboard.dto.AnswerCreateRequest;
import com.BugJava.EduConnect.qnaboard.dto.AnswerResponse;
import com.BugJava.EduConnect.qnaboard.dto.AnswerUpdateRequest;
import com.BugJava.EduConnect.qnaboard.entity.Answer;
import com.BugJava.EduConnect.qnaboard.entity.Question;
import com.BugJava.EduConnect.qnaboard.exception.AccessDeniedException;
import com.BugJava.EduConnect.qnaboard.exception.AnswerNotFoundException;
import com.BugJava.EduConnect.qnaboard.exception.QuestionNotFoundException;
import com.BugJava.EduConnect.qnaboard.repository.AnswerRepository;
import com.BugJava.EduConnect.qnaboard.repository.QuestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnswerService {

    private final AnswerRepository answerRepository;
    private final QuestionRepository questionRepository;
    private final UserRepository userRepository;

    @Transactional
    public void createAnswer(Long questionId, Long userId, AnswerCreateRequest req) {
        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("로그인 사용자 없음"));

        Question question = questionRepository.findById(questionId)
                .filter(q -> !q.isDeleted())
                .orElseThrow(() -> new QuestionNotFoundException(questionId.toString()));

        Answer answer = Answer.builder()
                .content(req.getContent())
                .user(user)
                .question(question)
                .isDeleted(false)
                .build();

        answerRepository.save(answer);
    }

    @Transactional(readOnly = true)
    public List<AnswerResponse> getAnswers(Long questionId) {
        if (!questionRepository.existsById(questionId)) {
            throw new QuestionNotFoundException(questionId.toString());
        }

        return answerRepository.findByQuestionIdAndIsDeletedFalse(questionId).stream()
                .map(AnswerResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public void updateAnswer(Long answerId, Long userId, AnswerUpdateRequest req) {
        Answer answer = answerRepository.findById(answerId)
                .filter(a -> !a.isDeleted())
                .orElseThrow(() -> new AnswerNotFoundException(answerId.toString()));

        if (!answer.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("본인의 답변만 수정할 수 있습니다.");
        }

        answer.change(req.getContent());
        answerRepository.save(answer);
    }

    @Transactional
    public void deleteAnswer(Long answerId, Long userId) {
        Answer answer = answerRepository.findById(answerId)
                .filter(a -> !a.isDeleted())
                .orElseThrow(() -> new AnswerNotFoundException(answerId.toString()));

        if (!answer.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("본인의 답변만 삭제할 수 있습니다.");
        }

        answer.softDelete();
        answerRepository.save(answer);
    }
}
