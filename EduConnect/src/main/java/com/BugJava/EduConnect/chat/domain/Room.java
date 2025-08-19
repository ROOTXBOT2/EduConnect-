package com.BugJava.EduConnect.chat.domain;

import com.BugJava.EduConnect.auth.entity.Users;
import com.BugJava.EduConnect.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Room extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Users 정보에 트랙 enums를 포함해서 대체
    private String code; // 고유한 방 번호 (고정)

    private String title; // 강의 제목

    public void updateTitle(String title) {
        this.title = title;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "instructor_id", nullable = false)
    private Users instructor; // 강사

    @Builder
    public Room(String code, String title, Users instructor) {
        this.code = code;
        this.title = title;
        this.instructor = instructor;
    }

}
