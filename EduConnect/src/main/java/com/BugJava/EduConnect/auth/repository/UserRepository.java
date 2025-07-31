package com.BugJava.EduConnect.auth.repository;

import com.BugJava.EduConnect.auth.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * @author rua
 */
@Repository
public interface UserRepository extends JpaRepository<Users, Long> {
    // 쿼리 메서드 선언만 하면 스프링이 자동 구현
    Optional<Users> findByEmail(String email);
    boolean existsByEmail(String email);
}
