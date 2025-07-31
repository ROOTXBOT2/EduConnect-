package com.BugJava.EduConnect.unit.service;

import com.BugJava.EduConnect.freeboard.domain.FbPost;
import com.BugJava.EduConnect.freeboard.dto.FbPostRequest;
import com.BugJava.EduConnect.freeboard.dto.FbPostResponse;
import com.BugJava.EduConnect.freeboard.exception.FbPostNotFoundException;
import com.BugJava.EduConnect.freeboard.repository.FbPostRepository;
import com.BugJava.EduConnect.freeboard.service.FbPostService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FbPostServiceTest {

    @Mock
    private FbPostRepository fbPostRepository;

    @InjectMocks
    private FbPostService fbPostService;

    @Test
    @DisplayName("게시글 전체 조회")
    void getAllPosts() {
        // given
        FbPost post1 = FbPost.builder().title("title1").content("content1").author("author1").build();
        FbPost post2 = FbPost.builder().title("title2").content("content2").author("author2").build();
        when(fbPostRepository.findAll()).thenReturn(Arrays.asList(post1, post2));

        // when
        List<FbPostResponse> allPosts = fbPostService.getAllPosts();

        // then
        assertThat(allPosts).hasSize(2);
        assertThat(allPosts.get(0).getTitle()).isEqualTo("title1");
        assertThat(allPosts.get(1).getTitle()).isEqualTo("title2");
    }

    @Test
    @DisplayName("게시글 단건 조회")
    void getPost() {
        // given
        Long postId = 1L;
        FbPost post = FbPost.builder().title("title").content("content").author("author").build();
        when(fbPostRepository.findById(postId)).thenReturn(Optional.of(post));

        // when
        FbPostResponse foundPost = fbPostService.getPost(postId);

        // then
        assertThat(foundPost.getTitle()).isEqualTo("title");
    }

    @Test
    @DisplayName("존재하지 않는 게시글 단건 조회 시 예외 발생")
    void getPost_notFound() {
        // given
        Long postId = 1L;
        when(fbPostRepository.findById(postId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> fbPostService.getPost(postId))
                .isInstanceOf(FbPostNotFoundException.class);
    }

    @Test
    @DisplayName("게시글 생성")
    void createPost() {
        // given
        FbPostRequest request = new FbPostRequest("title", "content", "author");
        FbPost post = FbPost.builder().title("title").content("content").author("author").build();
        when(fbPostRepository.save(any(FbPost.class))).thenReturn(post);

        // when
        FbPostResponse createdPost = fbPostService.createPost(request);

        // then
        assertThat(createdPost.getTitle()).isEqualTo("title");
    }

    @Test
    @DisplayName("게시글 수정")
    void updatePost() {
        // given
        Long postId = 1L;
        FbPostRequest request = new FbPostRequest("new title", "new content", "new author");
        FbPost post = FbPost.builder().title("title").content("content").author("author").build();
        when(fbPostRepository.findById(postId)).thenReturn(Optional.of(post));

        // when
        FbPostResponse updatedPost = fbPostService.updatePost(postId, request);

        // then
        assertThat(updatedPost.getTitle()).isEqualTo("new title");
        assertThat(updatedPost.getContent()).isEqualTo("new content");
    }

    @Test
    @DisplayName("게시글 삭제")
    void deletePost() {
        // given
        Long postId = 1L;
        FbPost post = FbPost.builder().title("title").content("content").author("author").build();
        when(fbPostRepository.findById(postId)).thenReturn(Optional.of(post));

        // when
        fbPostService.deletePost(postId);

        // then
        verify(fbPostRepository).delete(post);
    }
}
