package com.pjy008008.j_community.controller.dto;

import jakarta.validation.constraints.NotBlank;

public record PostCreateRequest(
        @NotBlank(message = "제목은 필수입니다.")
        String title,

        @NotBlank(message = "내용은 필수입니다.")
        String content,

        @NotBlank(message = "커뮤니티 이름은 필수입니다.")
        String communityName

        // 추후 이미지 관리도 추가
) {}