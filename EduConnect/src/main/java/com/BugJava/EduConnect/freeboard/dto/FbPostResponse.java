package com.BugJava.EduConnect.freeboard.dto;

import com.BugJava.EduConnect.freeboard.domain.FbPost;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FbPostResponse {
    private Long id;
    private String title;
    private String content;
    private String authorName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<FbCommentResponse> comments;

    public static FbPostResponse from(FbPost post) {
        List<FbCommentResponse> commentResponses = post.getComments().stream()
                .map(FbCommentResponse::from)
                .collect(Collectors.toList());

        return FbPostResponse.builder()
                .id(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .authorName(post.getUser().getName())
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .comments(commentResponses)
                .build();
    }

    public static FbPostResponse fromWithoutComments(FbPost post) {
        return FbPostResponse.builder()
                .id(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .authorName(post.getUser().getName())
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .build();
    }
}