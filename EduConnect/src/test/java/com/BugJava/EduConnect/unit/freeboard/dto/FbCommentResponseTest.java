package com.BugJava.EduConnect.unit.freeboard.dto;

import com.BugJava.EduConnect.auth.entity.Users;
import com.BugJava.EduConnect.freeboard.domain.FbComment;
import com.BugJava.EduConnect.freeboard.domain.FbPost;
import com.BugJava.EduConnect.freeboard.dto.FbCommentResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FbCommentResponseTest {

    @Test
    @DisplayName("FbComment 엔티티를 FbCommentResponse DTO로 변환")
    void from_converts_comment_entity_to_dto() {
        // given
        Users user = Users.builder().id(1L).name("testUser").build();
        FbPost post = FbPost.builder().id(1L).title("test post").build();
        FbComment comment = FbComment.builder()
                .id(1L)
                .content("test content")
                .user(user)
                .post(post)
                .build();

        // when
        FbCommentResponse response = FbCommentResponse.from(comment);

        // then
        assertThat(response.getId()).isEqualTo(comment.getId());
        assertThat(response.getContent()).isEqualTo(comment.getContent());
        assertThat(response.getAuthorName()).isEqualTo(user.getName());
        // assertThat(response.getPostId()).isEqualTo(post.getId()); // Removed
        // JPA Auditing이 동작하지 않는 단위 테스트 환경에서는 BaseEntity의 필드가 null이 됩니다.
        assertThat(response.getCreatedAt()).isNull();
        assertThat(response.getUpdatedAt()).isNull();
    }
}