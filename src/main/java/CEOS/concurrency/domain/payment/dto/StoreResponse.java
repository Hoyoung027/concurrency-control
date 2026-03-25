package CEOS.concurrency.domain.payment.dto;

import CEOS.concurrency.domain.payment.entity.Store;

import java.time.LocalDateTime;

public record StoreResponse(
        String githubId,
        String apiSecretKey,
        LocalDateTime createdAt
) {
    public static StoreResponse from(Store store) {
        return new StoreResponse(
                store.getGithubId(),
                store.getApiSecretKey(),
                store.getCreatedAt()
        );
    }
}
