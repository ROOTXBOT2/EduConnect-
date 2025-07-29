package com.BugJava.EduConnect.post.controller;

import com.BugJava.EduConnect.post.dto.PostRequest;
import com.BugJava.EduConnect.post.dto.PostResponse;
import com.BugJava.EduConnect.post.service.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/posts")
public class PostController {

    private final PostService postService;

    /** 전체 게시글 조회 */
    @GetMapping
    public ResponseEntity<List<PostResponse>> getAllPosts() {
        return ResponseEntity.ok(postService.getAllPosts());
    }

    /** 단일 게시글 조회 */
    @GetMapping("/{id}")
    public ResponseEntity<PostResponse> getPost(@PathVariable Long id) {
        return ResponseEntity.ok(postService.getPost(id));
    }

    /** 게시글 생성 */
    @PostMapping
    public ResponseEntity<?> createPost(@RequestBody @Valid PostRequest request,
                                        BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            String errorMessage = bindingResult.getFieldErrors().stream()
                    .map(error -> error.getField() + ": " + error.getDefaultMessage())
                    .reduce((msg1, msg2) -> msg1 + "; " + msg2)
                    .orElse("잘못된 요청입니다.");
            return ResponseEntity.badRequest().body(errorMessage);
        }

        PostResponse created = postService.createPost(request);
        return ResponseEntity.created(URI.create("/api/posts/" + created.getId()))
                .body(created);
    }

    /** 게시글 수정 */
    @PutMapping("/{id}")
    public ResponseEntity<?> updatePost(@PathVariable Long id,
                                        @RequestBody @Valid PostRequest request,
                                        BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            String errorMessage = bindingResult.getFieldErrors().stream()
                    .map(error -> error.getField() + ": " + error.getDefaultMessage())
                    .reduce((msg1, msg2) -> msg1 + "; " + msg2)
                    .orElse("잘못된 요청입니다.");
            return ResponseEntity.badRequest().body(errorMessage);
        }

        return ResponseEntity.ok(postService.updatePost(id, request));
    }

    /** 게시글 삭제 */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(@PathVariable Long id) {
        postService.deletePost(id);
        return ResponseEntity.noContent().build();
    }
}

