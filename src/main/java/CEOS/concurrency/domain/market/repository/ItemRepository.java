package CEOS.concurrency.domain.market.repository;

import CEOS.concurrency.domain.market.entity.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ItemRepository extends JpaRepository<Item, Long> {

    @Modifying
    @Query("UPDATE Item i SET i.quantity = :quantity WHERE i.id = :id")
    void resetQuantity(@Param("id") Long id, @Param("quantity") int quantity);

    @Modifying
    @Query(value = "INSERT INTO item (id, name, price, quantity, created_at, updated_at) " +
            "VALUES (1, 'ChatGPT Pro 50% 할인 이용권', 10000, :quantity, NOW(), NOW())", nativeQuery = true)
    void insertItemWithId(@Param("quantity") int quantity);
}
