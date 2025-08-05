package com.BugJava.EduConnect.unit.dto;

import com.BugJava.EduConnect.auth.entity.Users;
import com.BugJava.EduConnect.freeboard.domain.FbComment;
import com.BugJava.EduConnect.freeboard.domain.FbPost;
import com.BugJava.EduConnect.freeboard.dto.FbPostResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

class FbPostResponseTest {

    @Test
    @DisplayName("댓글을 포함하여 FbPost 엔티티를 FbPostResponse DTO로 변환")
    void from_with_comments_converts_post_entity_to_dto() {
        // given
        Users user = Users.builder().id(1L).name("testUser").build();
        FbPost post = FbPost.builder()
                .id(1L)
                .title("test post")
                .content("test content")
                .user(user)
                .build();
        FbComment comment = FbComment.builder().id(1L).content("test comment").user(user).post(post).build();
        post.addComment(comment);

        // when
        FbPostResponse response = FbPostResponse.from(post);

        // then
        assertThat(response.getId()).isEqualTo(post.getId());
        assertThat(response.getTitle()).isEqualTo(post.getTitle());
        assertThat(response.getContent()).isEqualTo(post.getContent());
        assertThat(response.getAuthorName()).isEqualTo(user.getName());
        assertThat(response.getComments()).hasSize(1);
        assertThat(response.getComments().get(0).getContent()).isEqualTo(comment.getContent());
    }

    @Test
    @DisplayName("댓글 없이 FbPost 엔티티를 FbPostResponse DTO로 변환")
    void from_without_comments_converts_post_entity_to_dto() {
        // given
        Users user = Users.builder().id(1L).name("testUser").build();
        FbPost post = FbPost.builder()
                .id(1L)
                .title("test post")
                .content("test content")
                .user(user)
                .comments(Collections.emptyList()) // 명시적으로 빈 리스트 설정
                .build();

        // when
        FbPostResponse response = FbPostResponse.fromWithoutComments(post);

        // then
        assertThat(response.getId()).isEqualTo(post.getId());
        assertThat(response.getTitle()).isEqualTo(post.getTitle());
        assertThat(response.getAuthorName()).isEqualTo(user.getName());
        assertThat(response.getComments()).isNull(); // fromWithoutComments는 comments 필드를 null로 둠
    }
}
