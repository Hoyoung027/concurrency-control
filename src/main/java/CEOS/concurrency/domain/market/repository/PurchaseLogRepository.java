package CEOS.concurrency.domain.market.repository;

import CEOS.concurrency.domain.market.entity.Item;
import CEOS.concurrency.domain.market.entity.PurchaseLog;
import CEOS.concurrency.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PurchaseLogRepository extends JpaRepository<PurchaseLog, Long> {

    long countByItem(Item item);

    @Query("SELECT p.member.nickname, COUNT(p) FROM PurchaseLog p WHERE p.item = :item GROUP BY p.member")
    List<Object[]> countByItemGroupByMember(@Param("item") Item item);
}
