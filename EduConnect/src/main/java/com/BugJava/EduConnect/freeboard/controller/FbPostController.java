package com.BugJava.EduConnect.freeboard.controller;

import com.BugJava.EduConnect.common.dto.ApiResponse;
import com.BugJava.EduConnect.common.dto.PageResponse;
import com.BugJava.EduConnect.freeboard.dto.FbPostRequest;
import com.BugJava.EduConnect.freeboard.dto.FbPostResponse;
import com.BugJava.EduConnect.freeboard.service.FbPostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
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

    /** 전체 게시글 조회 (페이징, 정렬, 검색 포함) */
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<FbPostResponse>>> getAllPosts(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            @RequestParam(required = false) String searchType,
            @RequestParam(required = false) String searchKeyword) {

        PageResponse<FbPostResponse> posts = postService.getAllPosts(pageable, searchType, searchKeyword);
        return ResponseEntity.ok(ApiResponse.success(posts, "전체 게시글 조회 성공"));
    }

    /** 단일 게시글 조회 */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<FbPostResponse>> getPost(@PathVariable Long id) {
        FbPostResponse post = postService.getPost(id);
        return ResponseEntity.ok(ApiResponse.success(post, "게시글 조회 성공"));
    }

    /** 게시글 생성 */
    @PostMapping
    public ResponseEntity<ApiResponse<FbPostResponse>> createPost(@RequestBody @Valid FbPostRequest request,
                                                                  @AuthenticationPrincipal Long userId) {
        FbPostResponse created = postService.createPost(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(created, "게시글 생성 성공"));
    }

    /** 게시글 수정 */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<FbPostResponse>> updatePost(@PathVariable(name = "id") Long id,
                                                                  @RequestBody @Valid FbPostRequest request) {
        FbPostResponse updated = postService.updatePost(id, request);
        return ResponseEntity.ok(ApiResponse.success(updated, "게시글 수정 성공"));
    }

    /** 게시글 삭제 */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Object>> deletePost(@PathVariable(name = "id") Long id) {
        postService.deletePost(id);
        return ResponseEntity.ok(ApiResponse.success(null, "게시글이 삭제되었습니다."));
    }
}