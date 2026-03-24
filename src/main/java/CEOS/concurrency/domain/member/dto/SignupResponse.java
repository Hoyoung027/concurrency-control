package CEOS.concurrency.domain.member.dto;

import CEOS.concurrency.domain.member.entity.Member;

import java.util.UUID;

public record SignupResponse(
        UUID uuid,
        String nickname,
        String character
) {
    public static SignupResponse from(Member member) {
        return new SignupResponse(
                member.getUuid(),
                member.getNickname(),
                member.getCharacterType().getEmoji()
        );
    }
}