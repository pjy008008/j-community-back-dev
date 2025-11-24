package com.pjy008008.j_community.model;

public enum NotificationType {
    COMMENT("님이 회원님의 게시물에 댓글을 남겼습니다"),
    REPLY("님이 회원님의 댓글에 답글을 남겼습니다"),
    UPVOTE_POST("님이 회원님의 게시물을 추천했습니다"),
    UPVOTE_COMMENT("님이 회원님의 댓글을 추천했습니다");

    private final String actionText;

    NotificationType(String actionText) {
        this.actionText = actionText;
    }

    public String getActionText() {
        return actionText;
    }
}