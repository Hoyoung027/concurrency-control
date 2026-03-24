package CEOS.concurrency.domain.member.dto;

import CEOS.concurrency.common.enums.CharacterType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SignupRequest(
        @NotBlank @Size(max = 20) String nickname,
        @NotBlank String password,
        @NotNull CharacterType characterType
) {
}
