package com.BugJava.EduConnect.common.entity;
import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter; // Setter 임포트
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * @author rua
 */
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@Getter
public abstract class BaseEntity{
    @CreatedDate
    @Column(updatable = false)
    @Setter // createdAt에 Setter 추가
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Setter // updatedAt에 Setter 추가 (일관성을 위해)
    private LocalDateTime updatedAt;
}