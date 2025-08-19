package com.BugJava.EduConnect.freeboard.domain;

import com.BugJava.EduConnect.common.entity.BaseEntity;
import jakarta.persistence.*;
import com.BugJava.EduConnect.auth.entity.Users;
import lombok.*;

@Entity
@Table(name = "fb_comments")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class FbComment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Lob
    @Column(nullable = false)
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Users user;

    // 연관관계 설정 메서드 (옵션)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private FbPost post;
}