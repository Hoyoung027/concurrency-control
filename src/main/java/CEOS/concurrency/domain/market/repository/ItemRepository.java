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
}
