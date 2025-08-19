package com.BugJava.EduConnect.qnaboard.repository;
import com.BugJava.EduConnect.qnaboard.dto.QuestionSearchRequest;
import com.BugJava.EduConnect.qnaboard.entity.Question;
import org.springframework.data.jpa.domain.Specification;
import com.BugJava.EduConnect.auth.enums.Track;

/**
 * @author rua
 */

public final class QuestionSpecs {

    private QuestionSpecs() { } // 유틸 클래스

    // 공통: is_deleted = false
    public static Specification<Question> notDeleted() {
        return (root, query, cb) -> cb.isFalse(root.get("isDeleted"));
    }

    // track = :track
    public static Specification<Question> hasTrack(Track track) {
        return (root, query, cb) -> cb.equal(root.get("track"), track);
    }

    // lower(title) like %:keyword%
    public static Specification<Question> titleContains(String keyword) {
        return (root, query, cb) -> {
            String pattern = "%" + keyword.trim().toLowerCase() + "%";
            return cb.like(cb.lower(root.get("title")), pattern);
        };
    }

    // 단일 진입점: 검색 스펙 빌더
    public static Specification<Question> buildSearch(QuestionSearchRequest req) {
        Track track = (req != null ? req.getTrack() : null);
        String keyword = (req != null ? req.getKeyword() : null);

        Specification<Question> spec = notDeleted();
        
        if (track != null) {
            spec = spec.and(hasTrack(track));
        }
        
        if (keyword != null && !keyword.isBlank()) {
            spec = spec.and(titleContains(keyword));
        }
        
        return spec;
    }
}
