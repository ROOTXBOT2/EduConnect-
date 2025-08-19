package com.BugJava.EduConnect.assignment.domain;

import com.BugJava.EduConnect.auth.entity.Users;
import com.BugJava.EduConnect.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "assignment")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Assignment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 255, nullable = false)
    private String title;

    @Lob
    @Column(nullable = false)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Users user;

    @Builder.Default
    @OneToMany(mappedBy = "assignment", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AssignmentComment> comments = new ArrayList<>();

    public void addComment(AssignmentComment comment) {
        comments.add(comment);
        comment.setAssignment(this);
    }

    public void removeComment(AssignmentComment comment) {
        comments.remove(comment);
        comment.setAssignment(null);
    }

}
