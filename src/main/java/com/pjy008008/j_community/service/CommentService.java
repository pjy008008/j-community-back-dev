package com.pjy008008.j_community.service;

import com.pjy008008.j_community.controller.dto.CommentCreateRequest;
import com.pjy008008.j_community.controller.dto.CommentResponse;
import com.pjy008008.j_community.controller.dto.CommentUpdateRequest;
import com.pjy008008.j_community.entity.Comment;
import com.pjy008008.j_community.entity.CommentVote;
import com.pjy008008.j_community.entity.Post;
import com.pjy008008.j_community.entity.User;
import com.pjy008008.j_community.exception.ResourceNotFoundException;
import com.pjy008008.j_community.model.NotificationType;
import com.pjy008008.j_community.model.VoteType;
import com.pjy008008.j_community.repository.CommentRepository;
import com.pjy008008.j_community.repository.CommentVoteRepository;
import com.pjy008008.j_community.repository.PostRepository;
import com.pjy008008.j_community.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final CommentVoteRepository commentVoteRepository;
    private final NotificationService notificationService;

    public List<CommentResponse> getCommentsByPost(Long postId, String username) {
        if (!postRepository.existsById(postId)) {
            throw new ResourceNotFoundException("Post not found with id: " + postId);
        }
        List<Comment> topLevelComments = commentRepository.findByPostIdAndParentIsNullOrderByCreatedAtAsc(postId);

        Map<Long, VoteType> userVotes = Collections.emptyMap();

        if (username != null) {
            User user = userRepository.findByUsername(username)
                    .orElse(null);

            if (user != null) {
                List<CommentVote> votes = commentVoteRepository.findByUserIdAndComment_Post_Id(user.getId(), postId);

                userVotes = votes.stream()
                        .collect(Collectors.toMap(
                                v -> v.getComment().getId(),
                                CommentVote::getVoteType
                        ));
            }
        }

        Map<Long, VoteType> finalUserVotes = userVotes;
        return topLevelComments.stream()
                .map(comment -> CommentResponse.from(comment, finalUserVotes))
                .collect(Collectors.toList());
    }

    @Transactional
    public CommentResponse createTopLevelComment(Long postId, CommentCreateRequest request, String username) {
        User author = findUserByUsername(username);
        Post post = findPostById(postId);

        Comment newComment = Comment.builder()
                .content(request.content())
                .author(author)
                .post(post)
                .parent(null)
                .build();

        Comment savedComment = commentRepository.save(newComment);

        notificationService.send(
                post.getAuthor(),
                author,
                NotificationType.COMMENT,
                request.content()
        );
        return CommentResponse.from(savedComment);
    }

    @Transactional
    public CommentResponse createReply(Long parentCommentId, CommentCreateRequest request, String username) {
        User author = findUserByUsername(username);

        Comment parentComment = findCommentById(parentCommentId);

        Post post = parentComment.getPost();

        Comment reply = Comment.builder()
                .content(request.content())
                .author(author)
                .post(post)
                .parent(parentComment)
                .build();

        Comment savedReply = commentRepository.save(reply);

        notificationService.send(
                parentComment.getAuthor(),
                author,
                NotificationType.REPLY,
                request.content()
        );

        return CommentResponse.from(savedReply);
    }

    @Transactional
    public CommentResponse updateComment(Long commentId, CommentUpdateRequest request, String username) {
        Comment comment = findCommentById(commentId);

        validateAuthor(comment, username);

        comment.update(request.content());

        return CommentResponse.from(comment);
    }

    @Transactional
    public void deleteComment(Long commentId, String username) {
        Comment comment = findCommentById(commentId);

        validateAuthor(comment, username);

        commentRepository.delete(comment);
    }

    @Transactional
    public int voteComment(Long commentId, VoteType voteType, String username) {
        User user = findUserByUsername(username);
        Comment comment = findCommentById(commentId);

        Optional<CommentVote> existingVote = commentVoteRepository.findByUserIdAndCommentId(user.getId(), commentId);

        if (existingVote.isPresent()) {
            CommentVote vote = existingVote.get();

            if (vote.getVoteType() == voteType) {
                comment.updateVotes(-voteType.getValue());
                commentVoteRepository.delete(vote);
            } else {
                comment.updateVotes(-vote.getVoteType().getValue() + voteType.getValue());
                vote.updateVoteType(voteType);
            }
        } else {
            comment.updateVotes(voteType.getValue());
            CommentVote newVote = CommentVote.builder()
                    .user(user)
                    .comment(comment)
                    .voteType(voteType)
                    .build();
            commentVoteRepository.save(newVote);

            if (voteType == VoteType.UP) {
                notificationService.send(
                        comment.getAuthor(),
                        user,
                        NotificationType.UPVOTE_COMMENT,
                        comment.getContent()
                );
            }
        }

        return comment.getVotes();
    }

    private void validateAuthor(Comment comment, String username) {
        if (!comment.getAuthor().getUsername().equals(username)) {
            throw new AccessDeniedException("You are not the author of this comment.");
        }
    }

    private User findUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }

    private Post findPostById(Long postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found: " + postId));
    }

    private Comment findCommentById(Long commentId) {
        return commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found: " + commentId));
    }
}