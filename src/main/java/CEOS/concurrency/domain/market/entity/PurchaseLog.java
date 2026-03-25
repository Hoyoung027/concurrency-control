package CEOS.concurrency.domain.market.entity;

import CEOS.concurrency.common.entity.BaseEntity;
import CEOS.concurrency.domain.member.entity.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "purchase_log")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PurchaseLog extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Builder
    public PurchaseLog(Item item, Member member) {
        this.item = item;
        this.member = member;
    }
}
