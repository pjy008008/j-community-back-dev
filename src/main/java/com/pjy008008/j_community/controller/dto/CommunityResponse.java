package com.pjy008008.j_community.controller.dto;

import com.pjy008008.j_community.entity.Community;

import java.time.LocalDateTime;

public record CommunityResponse(
        Long id,
        String name,
        String description,
        String colorTheme,
        String creatorName,
        LocalDateTime createdAt,
        int postCount,
        int memberCount
) {
    public static CommunityResponse from(Community community) {
        return new CommunityResponse(
                community.getId(),
                community.getName(),
                community.getDescription(),
                community.getColorTheme().getCssClass(),
                community.getCreator().getUsername(),
                community.getCreatedAt(),
                community.getPosts().size(),
                community.getMembers().size()
        );
    }
}