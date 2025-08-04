package com.BugJava.EduConnect.freeboard.controller;

import com.BugJava.EduConnect.freeboard.dto.FbPostRequest;
import com.BugJava.EduConnect.freeboard.dto.FbPostResponse;
import com.BugJava.EduConnect.freeboard.service.FbPostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/posts")
public class FbPostController {

    private final FbPostService postService;

    /** 전체 게시글 조회 */
    @GetMapping
    public ResponseEntity<List<FbPostResponse>> getAllPosts() {
        return ResponseEntity.ok(postService.getAllPosts());
    }

    /** 단일 게시글 조회 */
    @GetMapping("/{id}")
    public ResponseEntity<FbPostResponse> getPost(@PathVariable Long id) {
        return ResponseEntity.ok(postService.getPost(id));
    }

    /** 게시글 생성 */
    @PostMapping
    public ResponseEntity<FbPostResponse> createPost(@RequestBody @Valid FbPostRequest request,
                                                     @AuthenticationPrincipal Long userId) {
        FbPostResponse created = postService.createPost(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /** 게시글 수정 */
    @PutMapping("/{id}")
    public ResponseEntity<FbPostResponse> updatePost(@PathVariable Long id,
                                                     @RequestBody @Valid FbPostRequest request,
                                                     @AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(postService.updatePost(id, request, userId));
    }

    /** 게시글 삭제 */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(@PathVariable Long id,
                                           @AuthenticationPrincipal Long userId) {
        postService.deletePost(id, userId);
        return ResponseEntity.noContent().build();
    }
}