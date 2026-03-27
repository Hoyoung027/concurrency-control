package CEOS.concurrency.domain.market.controller;

import CEOS.concurrency.common.code.SuccessCode;
import CEOS.concurrency.common.response.Response;
import CEOS.concurrency.domain.market.dto.ItemResponse;
import CEOS.concurrency.domain.market.dto.PurchaseResponse;
import CEOS.concurrency.domain.market.dto.StatResponse;
import CEOS.concurrency.domain.market.service.ItemService;
import CEOS.concurrency.domain.member.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class ItemController {

    private final ItemService itemService;

    @GetMapping("/market/item/all")
    public ResponseEntity<Response<List<ItemResponse>>> getItems() {
        return ResponseEntity.ok(Response.of(SuccessCode.GET_SUCCESS, itemService.getItems(), "상품 전체 조회 API"));
    }

    @GetMapping("/market/item")
    public ResponseEntity<Response<ItemResponse>> getItem(@RequestParam Long itemId) {
        return ResponseEntity.ok(Response.of(SuccessCode.GET_SUCCESS, itemService.getItem(itemId), "상품 조회 API"));
    }

    @GetMapping("/market/stats")
    public ResponseEntity<Response<StatResponse>> getStats() {
        return ResponseEntity.ok(Response.of(SuccessCode.GET_SUCCESS, itemService.getStat(), "상품 구매 내역 통계 API"));
    }

    @PostMapping("/market/item/purchase")
    public ResponseEntity<Response<PurchaseResponse>> purchase(
            @RequestParam Long itemId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(Response.of(SuccessCode.OK, itemService.purchase(itemId, userDetails.getUuid()), "상품 구매 API"));
    }

    @PostMapping("/market/item/reset")
    public ResponseEntity<Response<Void>> reset() {
        itemService.reset();
        return ResponseEntity.ok(Response.of(SuccessCode.OK, null, "초기화 API"));
    }
}
