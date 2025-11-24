package com.pjy008008.j_community.service;

import com.pjy008008.j_community.controller.dto.PostCreateRequest;
import com.pjy008008.j_community.controller.dto.PostResponse;
import com.pjy008008.j_community.controller.dto.PostUpdateRequest;
import com.pjy008008.j_community.entity.Community;
import com.pjy008008.j_community.entity.Post;
import com.pjy008008.j_community.entity.PostVote;
import com.pjy008008.j_community.entity.User;
import com.pjy008008.j_community.exception.ResourceNotFoundException;
import com.pjy008008.j_community.model.NotificationType;
import com.pjy008008.j_community.model.VoteType;
import com.pjy008008.j_community.repository.CommunityRepository;
import com.pjy008008.j_community.repository.PostRepository;
import com.pjy008008.j_community.repository.PostVoteRepository;
import com.pjy008008.j_community.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.access.AccessDeniedException;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final CommunityRepository communityRepository;
    private final PostVoteRepository postVoteRepository;
    private final NotificationService notificationService;

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

    @Transactional
    public PostResponse updatePost(Long postId, PostUpdateRequest request, String username) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with id: " + postId));

        validateAuthor(post, username);

        post.update(request.title(), request.content());

        return PostResponse.from(post);
    }

    @Transactional
    public void deletePost(Long postId, String username) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with id: " + postId));

        validateAuthor(post, username);

        postRepository.delete(post);
    }

    @Transactional
    public int votePost(Long postId, VoteType voteType, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found: " + postId));

        Optional<PostVote> existingVote = postVoteRepository.findByUserIdAndPostId(user.getId(), postId);

        if (existingVote.isPresent()) {
            PostVote vote = existingVote.get();
            if (vote.getVoteType() == voteType) {
                post.updateVotes(-voteType.getValue());
                postVoteRepository.delete(vote);
            } else {
                post.updateVotes(-vote.getVoteType().getValue() + voteType.getValue());
                vote.updateVoteType(voteType);
            }
        } else {
            post.updateVotes(voteType.getValue());
            postVoteRepository.save(PostVote.builder().user(user).post(post).voteType(voteType).build());

            if (voteType == VoteType.UP) {
                notificationService.send(
                        post.getAuthor(),
                        user,
                        NotificationType.UPVOTE_POST,
                        post.getTitle()
                );
            }
        }
        return post.getVotes();
    }

    public Page<PostResponse> getAllPosts(Pageable pageable, String username) {
        Page<Post> posts = postRepository.findAllByOrderByCreatedAtDesc(pageable);
        return mapPostsToResponse(posts, username);
    }

    public Page<PostResponse> getPostsByCommunity(String communityName, Pageable pageable, String username) {
        Page<Post> posts = postRepository.findByCommunityName(communityName, pageable);
        return mapPostsToResponse(posts, username);
    }

    private Page<PostResponse> mapPostsToResponse(Page<Post> posts, String username) {
        Map<Long, VoteType> userVotes = Collections.emptyMap();

        if (username != null) {
            User user = userRepository.findByUsername(username).orElse(null);
            if (user != null) {
                List<Long> postIds = posts.getContent().stream().map(Post::getId).toList();
                List<PostVote> votes = postVoteRepository.findByUserIdAndPostIdIn(user.getId(), postIds);

                userVotes = votes.stream()
                        .collect(Collectors.toMap(
                                v -> v.getPost().getId(),
                                PostVote::getVoteType
                        ));
            }
        }

        Map<Long, VoteType> finalUserVotes = userVotes;
        return posts.map(post -> PostResponse.from(post, finalUserVotes.getOrDefault(post.getId(), null)));
    }

    private void validateAuthor(Post post, String username) {
        if (!post.getAuthor().getUsername().equals(username)) {
            throw new AccessDeniedException("You are not the author of this post.");
        }
    }
}