package com.BugJava.EduConnect.unit.service;

import com.BugJava.EduConnect.post.domain.Post;
import com.BugJava.EduConnect.post.dto.PostRequest;
import com.BugJava.EduConnect.post.dto.PostResponse;
import com.BugJava.EduConnect.post.exception.PostNotFoundException;
import com.BugJava.EduConnect.post.repository.PostRepository;
import com.BugJava.EduConnect.post.service.PostService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class PostServiceTest {

    private PostRepository postRepository;
    private PostService postService;

    @BeforeEach
    void setUp() {
        postRepository = mock(PostRepository.class);
        postService = new PostService(postRepository);
    }

    // 테스트용 Post 객체 생성 헬퍼 메서드
    private Post createPost(Long id, String title, String content, String author) {
        return Post.builder()
                .id(id)  // 테스트 목적이라 직접 id 주입
                .title(title)
                .content(content)
                .author(author)
                .build();
    }

    // 테스트용 PostRequest 생성 헬퍼 메서드
    private PostRequest createRequest(String title, String content, String author) {
        return PostRequest.builder()
                .title(title)
                .content(content)
                .author(author)
                .build();
    }

    @Test
    @DisplayName("게시글 생성 성공")
    void createPost_Success() {
        PostRequest request = createRequest("제목", "내용", "작성자");

        // 저장 시 id가 부여된 객체를 반환하도록 간단히 설정
        when(postRepository.save(any(Post.class)))
                .thenAnswer(invocation -> {
                    Post post = invocation.getArgument(0);
                    return createPost(1L, post.getTitle(), post.getContent(), post.getAuthor());
                });

        PostResponse response = postService.createPost(request);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getTitle()).isEqualTo("제목");
        assertThat(response.getAuthor()).isEqualTo("작성자");

        verify(postRepository, times(1)).save(any(Post.class));
    }

    @Test
    @DisplayName("게시글 단건 조회 성공")
    void getPost_Success() {
        Post post = createPost(1L, "제목", "내용", "작성자");

        when(postRepository.findById(1L)).thenReturn(Optional.of(post));

        PostResponse response = postService.getPost(1L);

        assertThat(response.getTitle()).isEqualTo("제목");

        verify(postRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("게시글 단건 조회 실패 - 존재하지 않는 ID")
    void getPost_Fail_NotFound() {
        when(postRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> postService.getPost(1L))
                .isInstanceOf(PostNotFoundException.class);

        verify(postRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("게시글 수정 성공")
    void updatePost_Success() {
        Post post = createPost(1L, "기존 제목", "기존 내용", "작성자");

        PostRequest request = createRequest("수정된 제목", "수정된 내용", "작성자");

        when(postRepository.findById(1L)).thenReturn(Optional.of(post));

        PostResponse response = postService.updatePost(1L, request);

        assertThat(response.getTitle()).isEqualTo("수정된 제목");
        assertThat(response.getContent()).isEqualTo("수정된 내용");
        assertThat(response.getAuthor()).isEqualTo("작성자");

        verify(postRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("게시글 삭제 성공")
    void deletePost_Success() {
        Post post = createPost(1L, "제목", "내용", "작성자");
        when(postRepository.findById(1L)).thenReturn(Optional.of(post));

        postService.deletePost(1L);

        verify(postRepository, times(1)).findById(1L);
        verify(postRepository, times(1)).delete(post);
    }
}


