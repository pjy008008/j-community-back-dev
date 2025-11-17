package com.pjy008008.j_community.controller.dto;

import com.pjy008008.j_community.entity.Comment;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public record CommentResponse(
        Long id,
        String author,
        String content,
        int votes,
        LocalDateTime createdAt,
        List<CommentResponse> replies
) {
    public static CommentResponse from(Comment comment) {
        List<CommentResponse> replyDtos = comment.getReplies().stream()
                .map(CommentResponse::from)
                .collect(Collectors.toList());

        return new CommentResponse(
                comment.getId(),
                comment.getAuthor().getUsername(),
                comment.getContent(),
                comment.getVotes(),
                comment.getCreatedAt(),
                replyDtos
        );
    }
}