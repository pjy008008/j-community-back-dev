package com.pjy008008.j_community.controller.dto;

import jakarta.validation.constraints.NotBlank;

public record WithdrawalRequest(
        @NotBlank(message = "비밀번호를 입력해주세요.")
        String password
) {}
