package com.pjy008008.j_community.controller.dto;
import com.pjy008008.j_community.entity.Post;

import java.time.LocalDateTime;

public record PostResponse(
        Long id,
        String title,
        String content,
        String author,
        String authorInitial,
        String community,
        String communityColor,
        int votes,
        int commentCount,
        LocalDateTime createdAt
) {
    public static PostResponse from(Post post) {
        String username = post.getAuthor().getUsername();
        return new PostResponse(
                post.getId(),
                post.getTitle(),
                post.getContent(),
                username,
                username.substring(0, 1),
                post.getCommunity().getName(),
                post.getCommunity().getColorTheme().getCssClass(),
                post.getVotes(),
                post.getComments().size(),
                post.getCreatedAt()
        );
    }
}
