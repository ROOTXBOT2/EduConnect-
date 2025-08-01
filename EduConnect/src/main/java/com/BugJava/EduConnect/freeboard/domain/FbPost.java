package com.BugJava.EduConnect.freeboard.domain;

import jakarta.persistence.*;
import com.BugJava.EduConnect.auth.entity.Users;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "posts")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class FbPost {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String title;

    @Lob
    @Column(nullable = false)
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Users user;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    // 추후 댓글, 대댓글 연관관계는 여기에 추가
    @Builder.Default
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FbComment> comments = new ArrayList<>();

    // 연관관계 편의 메서드
    public void addComment(FbComment comment) {
        comments.add(comment);
        comment.setPost(this);
    }

    public void removeComment(FbComment comment) {
        comments.remove(comment);
        comment.setPost(null);
    }


    // 추후 파일 연관관계는 여기에 추가

}

