package com.BugJava.EduConnect.qnaboard.entity;

import com.BugJava.EduConnect.auth.entity.Users;
import com.BugJava.EduConnect.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * @author rua
 */

@Entity
@Table(name = "answers")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Answer extends BaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 답변 내용
    private String content;

    @Column(nullable = false)
    private boolean isDeleted;

    @Column
    private LocalDateTime deletedAt;

    // 답변 작성자
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Users user;

    // 어떤 질문에 달린 답변인지
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @OneToMany(mappedBy = "answer")
    private List<Comment> comments = new ArrayList<>();

    public void change(String content) {
        this.content = content;
    }

    public void softDelete() {
        this.isDeleted = true;
        this.deletedAt = LocalDateTime.now();
    }
}
