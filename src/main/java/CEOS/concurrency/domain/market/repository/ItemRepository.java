package CEOS.concurrency.domain.market.repository;

import CEOS.concurrency.domain.market.entity.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ItemRepository extends JpaRepository<Item, Long> {

    @Modifying
    @Query("UPDATE Item i SET i.stock = :stock WHERE i.id = :id")
    void resetStock(@Param("id") Long id, @Param("stock") int stock);

    @Modifying
    @Query(value = "INSERT INTO item (id, name, price, stock, created_at, updated_at) " +
            "VALUES (1, 'ChatGPT Pro 50% 할인 이용권', 10000, :stock, NOW(), NOW())", nativeQuery = true)
    void insertItemWithId(@Param("stock") int stock);
}
