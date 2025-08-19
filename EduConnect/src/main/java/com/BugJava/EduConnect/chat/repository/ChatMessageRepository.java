package com.BugJava.EduConnect.chat.repository;

import com.BugJava.EduConnect.chat.domain.ChatMessage;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import org.springframework.data.jpa.repository.EntityGraph;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    /**
     * ChatMessage 엔티티와 연관된 필드를 즉시 로딩(Eager fetch) 하도록 지정
     * N+1 문제를 방지하며 한 번의 쿼리로 관련된 연관 데이터도 같이 조회
     */
    @EntityGraph(attributePaths = {"chatCodeBlock", "chatAttachment", "sender"})

    /**
     * ChatMessage 엔티티를 조회 (메시지 ID가 lastMessageId보다 작은 것들만 조회, 메시지 ID 기준 내림차순 정렬)
     * PageRequest 객체를 받아서 쿼리 결과에 페이징을 적용 (최대 size 만큼만 결과를 반환)
     */
    @Query("SELECT cm FROM ChatMessage cm WHERE cm.chatSession.id = :sessionId AND cm.id < :lastMessageId ORDER BY cm.id DESC")

    /**
     * Slice는 Page와 유사하지만 전체 페이지 개수 계산 없이 다음 데이터가 더 있는지만 체크하는 경량 페이징 결과.
     * 역방향 무한 스크롤에서는 전체 카운트가 필요 없으므로 Slice가 적합
     */
    Slice<ChatMessage> findBySessionIdWithPaging(@Param("sessionId") Long sessionId, @Param("lastMessageId") Long lastMessageId, Pageable pageable);
}
