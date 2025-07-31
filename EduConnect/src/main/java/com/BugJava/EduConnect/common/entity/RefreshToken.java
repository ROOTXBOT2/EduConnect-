package com.BugJava.EduConnect.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * @author rua
 */
@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class RefreshToken {
    @Id
    private String username; // 사용자 계정명(또는 userId, PK)

    @Column(nullable = false)
    private String token;

    @Column(nullable = false)
    private Long expiry; // 만료시각(타임스탬프, ms)
}
