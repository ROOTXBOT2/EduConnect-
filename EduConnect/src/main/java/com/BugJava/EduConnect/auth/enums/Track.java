package com.BugJava.EduConnect.auth.enums;

import java.util.Arrays;

/**
 * @author rua
 */
public enum Track {
    BACKEND, FRONTEND, FULLSTACK;
    public static Track fromString(String str) {
        //로그로 남길 곳
        if (str == null) {
            throw new IllegalArgumentException("Track cannot be null.");
        }
        return Arrays.stream(Track.values())
                .filter(t -> t.name().equalsIgnoreCase(str))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 트랙입니다: " + str));
    }
}
