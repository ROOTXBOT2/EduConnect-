package com.BugJava.EduConnect.util;

import com.BugJava.EduConnect.auth.entity.Users;
import com.BugJava.EduConnect.auth.enums.Role;
import com.BugJava.EduConnect.auth.enums.Track;
import com.BugJava.EduConnect.qnaboard.dto.QuestionCreateRequest;
import com.BugJava.EduConnect.qnaboard.dto.QuestionSearchRequest;
import com.BugJava.EduConnect.qnaboard.entity.Question;

import java.time.LocalDateTime;

/**
 * QnA 테스트 데이터 빌더 유틸리티 클래스
 * 
 * @author rua
 */
public final class QnaTestDataBuilder {

    private QnaTestDataBuilder() {
        // 유틸리티 클래스
    }

    /**
     * 테스트용 사용자 빌더
     */
    public static class UserBuilder {
        private String email = "test@example.com";
        private String password = "password123";
        private String name = "테스트사용자";
        private Role role = Role.STUDENT;
        private Track track = Track.BACKEND;
        private boolean isDeleted = false;
        private LocalDateTime deletedAt;

        public UserBuilder email(String email) {
            this.email = email;
            return this;
        }

        public UserBuilder password(String password) {
            this.password = password;
            return this;
        }

        public UserBuilder name(String name) {
            this.name = name;
            return this;
        }

        public UserBuilder role(Role role) {
            this.role = role;
            return this;
        }

        public UserBuilder track(Track track) {
            this.track = track;
            return this;
        }

        public UserBuilder deleted() {
            this.isDeleted = true;
            this.deletedAt = LocalDateTime.now();
            return this;
        }

        public Users build() {
            return Users.builder()
                    .email(email)
                    .password(password)
                    .name(name)
                    .role(role)
                    .track(track)
                    .isDeleted(isDeleted)
                    .deletedAt(deletedAt)
                    .build();
        }
    }

    /**
     * 테스트용 질문 빌더
     */
    public static class QuestionBuilder {
        private Users user;
        private String title = "테스트 질문";
        private String content = "테스트 내용입니다.";
        private Track track = Track.BACKEND;
        private boolean isDeleted = false;
        private LocalDateTime deletedAt;

        public QuestionBuilder user(Users user) {
            this.user = user;
            return this;
        }

        public QuestionBuilder title(String title) {
            this.title = title;
            return this;
        }

        public QuestionBuilder content(String content) {
            this.content = content;
            return this;
        }

        public QuestionBuilder track(Track track) {
            this.track = track;
            return this;
        }

        public QuestionBuilder deleted() {
            this.isDeleted = true;
            this.deletedAt = LocalDateTime.now();
            return this;
        }

        public Question build() {
            if (user == null) {
                throw new IllegalStateException("User must be set for Question");
            }
            
            return Question.builder()
                    .user(user)
                    .title(title)
                    .content(content)
                    .track(track)
                    .isDeleted(isDeleted)
                    .deletedAt(deletedAt)
                    .build();
        }
    }

    /**
     * 테스트용 질문 생성 요청 빌더
     */
    public static class QuestionCreateRequestBuilder {
        private String title = "테스트 질문";
        private String content = "테스트 내용입니다.";
        private Track track = Track.BACKEND;

        public QuestionCreateRequestBuilder title(String title) {
            this.title = title;
            return this;
        }

        public QuestionCreateRequestBuilder content(String content) {
            this.content = content;
            return this;
        }

        public QuestionCreateRequestBuilder track(Track track) {
            this.track = track;
            return this;
        }

        public QuestionCreateRequest build() {
            QuestionCreateRequest request = new QuestionCreateRequest();
            request.setTitle(title);
            request.setContent(content);
            request.setTrack(track);
            return request;
        }
    }

    /**
     * 테스트용 질문 검색 요청 빌더
     */
    public static class QuestionSearchRequestBuilder {
        private Track track;
        private String keyword;

        public QuestionSearchRequestBuilder track(Track track) {
            this.track = track;
            return this;
        }

        public QuestionSearchRequestBuilder keyword(String keyword) {
            this.keyword = keyword;
            return this;
        }

        public QuestionSearchRequest build() {
            return QuestionSearchRequest.builder()
                    .track(track)
                    .keyword(keyword)
                    .build();
        }
    }

    // 정적 팩토리 메서드들
    public static UserBuilder aUser() {
        return new UserBuilder();
    }

    public static UserBuilder aStudent() {
        return new UserBuilder().role(Role.STUDENT);
    }

    public static UserBuilder anInstructor() {
        return new UserBuilder().role(Role.INSTRUCTOR);
    }

    public static UserBuilder aBackendStudent() {
        return new UserBuilder().role(Role.STUDENT).track(Track.BACKEND);
    }

    public static UserBuilder aFrontendStudent() {
        return new UserBuilder().role(Role.STUDENT).track(Track.FRONTEND);
    }

    public static QuestionBuilder aQuestion() {
        return new QuestionBuilder();
    }

    public static QuestionBuilder aBackendQuestion() {
        return new QuestionBuilder().track(Track.BACKEND);
    }

    public static QuestionBuilder aFrontendQuestion() {
        return new QuestionBuilder().track(Track.FRONTEND);
    }

    public static QuestionCreateRequestBuilder aQuestionCreateRequest() {
        return new QuestionCreateRequestBuilder();
    }

    public static QuestionSearchRequestBuilder aQuestionSearchRequest() {
        return new QuestionSearchRequestBuilder();
    }

    // 샘플 데이터 생성 도우미
    public static Users createSampleStudent(String email, String name, Track track) {
        return aStudent()
                .email(email)
                .name(name)
                .track(track)
                .build();
    }

    public static Users createSampleInstructor(String email, String name) {
        return anInstructor()
                .email(email)
                .name(name)
                .track(Track.TRACK_INSTRUCTOR)
                .build();
    }

    public static Question createSampleQuestion(Users user, String title, String content, Track track) {
        return aQuestion()
                .user(user)
                .title(title)
                .content(content)
                .track(track)
                .build();
    }

    public static QuestionCreateRequest createSampleQuestionRequest(String title, String content, Track track) {
        return aQuestionCreateRequest()
                .title(title)
                .content(content)
                .track(track)
                .build();
    }
}