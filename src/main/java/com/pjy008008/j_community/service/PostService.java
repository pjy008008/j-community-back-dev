package com.pjy008008.j_community.service;

import com.pjy008008.j_community.controller.dto.PostCreateRequest;
import com.pjy008008.j_community.controller.dto.PostResponse;
import com.pjy008008.j_community.entity.Community;
import com.pjy008008.j_community.entity.Post;
import com.pjy008008.j_community.entity.User;
import com.pjy008008.j_community.repository.CommunityRepository;
import com.pjy008008.j_community.repository.PostRepository;
import com.pjy008008.j_community.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final CommunityRepository communityRepository;

    @Transactional
    public PostResponse createPost(PostCreateRequest request, String username) {
        User author = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        Community community = communityRepository.findByName(request.communityName())
                .orElseThrow(() -> new IllegalArgumentException("Community not found: " + request.communityName()));

        Post post = Post.builder()
                .title(request.title())
                .content(request.content())
                .author(author)
                .community(community)
                .build();

        Post savedPost = postRepository.save(post);
        return PostResponse.from(savedPost);
    }

    public Page<PostResponse> getAllPosts(Pageable pageable) {
        Page<Post> posts = postRepository.findAllByOrderByCreatedAtDesc(pageable);
        return posts.map(PostResponse::from);
    }

    public Page<PostResponse> getPostsByCommunity(String communityName, Pageable pageable) {
        Page<Post> posts = postRepository.findByCommunityName(communityName, pageable);
        return posts.map(PostResponse::from);
    }
}