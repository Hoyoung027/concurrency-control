package CEOS.concurrency.domain.market.entity;

import CEOS.concurrency.common.code.BusinessErrorCode;
import CEOS.concurrency.common.entity.BaseEntity;
import CEOS.concurrency.common.exception.BusinessException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "item")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Item extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private int price;

    @Column(nullable = false)
    private int stock;

    @Builder
    public Item(String name, int price, int stock) {
        this.name = name;
        this.price = price;
        this.stock = stock;
    }

    public void decreaseStock() {
        if (this.stock <= 0) {
            throw new BusinessException(BusinessErrorCode.OUT_OF_STOCK);
        }
        this.stock -= 1;
    }

    public void resetStock(int stock) {
        this.stock = stock;
    }
}
