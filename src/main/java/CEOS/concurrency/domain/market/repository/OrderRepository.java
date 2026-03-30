package CEOS.concurrency.domain.market.repository;

import CEOS.concurrency.domain.market.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    @Query("SELECT o.member.nickname, o.member.characterType, COUNT(o) FROM Order o GROUP BY o.member")
    List<Object[]> countGroupByMember();
}
