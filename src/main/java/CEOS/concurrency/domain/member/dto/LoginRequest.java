package CEOS.concurrency.domain.member.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank String nickname,
        @NotBlank String password
) {
}
