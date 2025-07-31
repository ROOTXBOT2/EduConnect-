package com.BugJava.EduConnect.unit.service;

import com.BugJava.EduConnect.freeboard.domain.FbPost;
import com.BugJava.EduConnect.freeboard.dto.FbPostRequest;
import com.BugJava.EduConnect.freeboard.dto.FbPostResponse;
import com.BugJava.EduConnect.freeboard.exception.FbPostNotFoundException;
import com.BugJava.EduConnect.post.mapper.PostMapper;
import com.BugJava.EduConnect.freeboard.repository.FbPostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostService {

    private final FbPostRepository postRepository;
    private final PostMapper postMapper;

    public List<FbPostResponse> getAllPosts() {
        return postRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(postMapper::toResponse)
                .collect(Collectors.toList());
    }

    public FbPostResponse getPost(Long id) {
        FbPost post = postRepository.findById(id)
                .orElseThrow(() -> new FbPostNotFoundException(id));
        return postMapper.toResponse(post);
    }

    @Transactional
    public FbPostResponse createPost(FbPostRequest request) {
        FbPost post = FbPost.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .author(request.getAuthor())
                .build();
        return postMapper.toResponse(postRepository.save(post));
    }

    @Transactional
    public FbPostResponse updatePost(Long id, FbPostRequest request) {
        FbPost post = postRepository.findById(id)
                .orElseThrow(() -> new FbPostNotFoundException(id));
        post.update(request.getTitle(), request.getContent(), request.getAuthor());
        return postMapper.toResponse(post);
    }

    @Transactional
    public void deletePost(Long id) {
        FbPost post = postRepository.findById(id)
                .orElseThrow(() -> new FbPostNotFoundException(id));
        postRepository.delete(post);
    }
}



