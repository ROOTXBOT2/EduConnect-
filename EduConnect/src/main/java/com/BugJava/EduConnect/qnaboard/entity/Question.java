package com.BugJava.EduConnect.qnaboard.entity;

import com.BugJava.EduConnect.auth.entity.Users;
import com.BugJava.EduConnect.auth.enums.Track;
import com.BugJava.EduConnect.common.entity.BaseEntity;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * @author rua
 */

@Entity
@Table(name = "qna_questions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Question extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 질문 작성자
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Users user;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "VARCHAR(255)", nullable = false)
    private Track track; //질문을 전달할 곳. 각 트랙 학생, 강사. BACKEND,FRONTEND,FULLSTACK,TRACK_INSTRUCTOR

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(nullable = false)
    private boolean isDeleted;

    @Column
    private LocalDateTime deletedAt;

    @OneToMany(mappedBy = "question")
    private List<Answer> answers = new ArrayList<>();

    public void change(String title, String content, Track track) {
        this.title = title;
        this.content = content;
        this.track = track;
    }

    public void softDelete() {
        this.isDeleted = true;
        this.deletedAt = LocalDateTime.now();
    }
}
