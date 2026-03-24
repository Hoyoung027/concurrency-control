package CEOS.concurrency.domain.member.dto;

public record LoginResponse(
        String accessToken,
        String refreshToken
) {
}
