package com.BugJava.EduConnect.auth.entity;

import com.BugJava.EduConnect.auth.enums.Role;
import com.BugJava.EduConnect.auth.enums.Track;
import com.BugJava.EduConnect.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

import static com.BugJava.EduConnect.auth.enums.Role.STUDENT;

/**
 * @author rua
 */

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Users extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 100)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, length = 50)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role = STUDENT; // "STUDENT,INSTRUCTOR"

    @Column(nullable = false)
    private boolean isDeleted = false;

    @Column
    private LocalDateTime deletedAt = null;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Track track;

    public void updateProfile(String name, Track track) {
        this.name = name;
        this.track = track;
    }
}

