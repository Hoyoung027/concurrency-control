package CEOS.concurrency.domain.member.repository;

import CEOS.concurrency.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByNickname(String nickname);

    Optional<Member> findByUuid(java.util.UUID uuid);
}