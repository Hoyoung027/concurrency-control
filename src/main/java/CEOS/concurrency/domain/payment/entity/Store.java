package CEOS.concurrency.domain.payment.entity;

import CEOS.concurrency.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "store")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Store extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, updatable = false)
    private String githubId;

    @Column(nullable = false, unique = true, updatable = false)
    private String apiSecretKey;

    @Builder
    public Store(String githubId, String apiSecretKey) {
        this.githubId = githubId;
        this.apiSecretKey = apiSecretKey;
    }
}
