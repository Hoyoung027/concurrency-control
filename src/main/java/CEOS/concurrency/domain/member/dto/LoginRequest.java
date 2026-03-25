package CEOS.concurrency.domain.member.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "닉네임을 입력해주세요") String nickname,
        @NotBlank(message = "비밀번호를 입력해주세요") String password
) {
}
