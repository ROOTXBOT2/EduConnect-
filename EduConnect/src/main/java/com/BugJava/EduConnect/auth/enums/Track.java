package com.BugJava.EduConnect.auth.enums;

import java.util.Arrays;

/**
 * @author rua
 */
public enum Track {
    BACKEND, FRONTEND, FULLSTACK;
    public static Track fromString(String str) {
        return Arrays.stream(Track.values())
                .filter(t -> t.name().equalsIgnoreCase(str))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 트랙입니다: " + str));
    }
}
