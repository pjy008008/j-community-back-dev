package com.pjy008008.j_community.controller.dto;

import com.pjy008008.j_community.model.ColorTheme;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CommunityCreateRequest(
        @NotBlank(message = "커뮤니티 이름은 필수입니다.")
        @Size(max = 20, message = "커뮤니티 이름은 20자를 넘을 수 없습니다.")
        String name,

        @Size(max = 200, message = "설명은 200자를 넘을 수 없습니다.")
        String description,

        @NotNull(message = "컬러 테마는 필수입니다.")
        ColorTheme colorTheme
) {}