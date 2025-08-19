package com.BugJava.EduConnect.util;

import com.BugJava.EduConnect.auth.entity.Users;
import com.BugJava.EduConnect.auth.enums.Role;
import com.BugJava.EduConnect.chat.domain.ChatCodeBlock;
import com.BugJava.EduConnect.chat.domain.ChatMessage;
import com.BugJava.EduConnect.chat.domain.ChatSession;
import com.BugJava.EduConnect.chat.domain.Enrollment;
import com.BugJava.EduConnect.chat.domain.Room;
import com.BugJava.EduConnect.chat.enums.MessageType;

import java.lang.reflect.Field;

public class TestUtils {

    // --- Reflection Helper for setting ID ---
    private static void setEntityId(Object entity, Long id) {
        try {
            Field idField = entity.getClass().getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(entity, id);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("Failed to set entity ID for testing", e);
        }
    }

    // --- User Creation ---
    public static Users createUser(Long id, String name, Role role) {
        return Users.builder()
                .id(id)
                .email(name.toLowerCase() + "@example.com")
                .name(name)
                .role(role)
                .build();
    }

    // --- Room Creation ---
    public static Room createRoom(Long id, String code, String title, Users instructor) {
        Room room = Room.builder()
                .code(code)
                .title(title)
                .instructor(instructor)
                .build();
        setEntityId(room, id);
        return room;
    }

    // --- ChatSession Creation ---
    public static ChatSession createChatSession(Long id, String title, Room room) {
        ChatSession session = ChatSession.builder()
                .title(title)
                .room(room)
                .build();
        setEntityId(session, id);
        return session;
    }

    // --- ChatMessage Creation ---
    public static ChatMessage createChatMessage(Long id, ChatSession session, Users sender, String content, MessageType messageType) {
        ChatMessage message = ChatMessage.builder()
                .chatSession(session)
                .sender(sender)
                .content(content)
                .messageType(messageType)
                .build();
        setEntityId(message, id);
        return message;
    }

    // --- Enrollment Creation ---
    public static Enrollment createEnrollment(Long id, Users user, Room room) {
        Enrollment enrollment = Enrollment.builder()
                .user(user)
                .room(room)
                .build();
        setEntityId(enrollment, id);
        return enrollment;
    }

    // --- ChatCodeBlock Creation ---
    public static ChatCodeBlock createChatCodeBlock(Long id, ChatMessage message, String language, String code) {
        ChatCodeBlock codeBlock = ChatCodeBlock.builder()
                .chatMessage(message)
                .language(language)
                .codeContent(code)
                .build();
        setEntityId(codeBlock, id);
        return codeBlock;
    }
}