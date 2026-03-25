package CEOS.concurrency.domain.payment.entity;

import CEOS.concurrency.common.entity.BaseEntity;
import CEOS.concurrency.domain.payment.enums.Currency;
import CEOS.concurrency.domain.payment.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "payment_log", uniqueConstraints = {
        @UniqueConstraint(name = "uq_store_payment", columnNames = {"store_id", "paymentId"})
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PaymentLog extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, updatable = false)
    private String paymentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @Column(nullable = false)
    private String orderName;

    @Column(nullable = false)
    private Long amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Currency currency;

    @Column
    private String customData;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;

    @Builder
    public PaymentLog(String paymentId, Store store, String orderName,
                      Long amount, Currency currency, String customData, PaymentStatus status) {
        this.paymentId = paymentId;
        this.store = store;
        this.orderName = orderName;
        this.amount = amount;
        this.currency = currency;
        this.customData = customData;
        this.status = status;
    }
}
