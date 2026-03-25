package CEOS.concurrency.domain.payment.repository;

import CEOS.concurrency.domain.payment.entity.Store;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StoreRepository extends JpaRepository<Store, Long> {
    Optional<Store> findByGithubId(String githubId);
    Optional<Store> findById(Long id);
}
