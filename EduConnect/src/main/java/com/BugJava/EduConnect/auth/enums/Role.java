package com.BugJava.EduConnect.auth.enums;

import java.util.Arrays;

/**
 * @author rua
 */
public enum Role {
    STUDENT, INSTRUCTOR, ADMIN;

    public static Role fromString(String str) {
        if (str == null) {
            throw new IllegalArgumentException("Role cannot be null.");
        }
        return Arrays.stream(Role.values())
                .filter(t -> t.name().equalsIgnoreCase(str))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 권한입니다: " + str));
    }
}
