package CEOS.concurrency.domain.member.dto;

import CEOS.concurrency.common.enums.CharacterType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SignupRequest(
        @NotBlank(message = "닉네임을 입력해주세요 (최대 20자)") @Size(max = 20) String nickname,
        @NotBlank(message = "비밀번호를 입력해주세요") String password,
        @NotNull(message = "캐릭터를 선택해주세요") CharacterType characterType
) {
}
