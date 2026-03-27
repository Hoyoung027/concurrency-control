package CEOS.concurrency.domain.market.controller;

import CEOS.concurrency.common.code.SuccessCode;
import CEOS.concurrency.common.response.Response;
import CEOS.concurrency.domain.market.dto.ItemResponse;
import CEOS.concurrency.domain.market.dto.PurchaseResponse;
import CEOS.concurrency.domain.market.service.ItemService;
import CEOS.concurrency.domain.member.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class ItemController {

    private final ItemService itemService;

    @GetMapping("/market/item")
    public ResponseEntity<Response<ItemResponse>> getItem() {
        return ResponseEntity.ok(Response.of(SuccessCode.GET_SUCCESS, itemService.getItem(), "상품 조회 API"));
    }

    @PostMapping("/market/item/purchase")
    public ResponseEntity<Response<PurchaseResponse>> purchase(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(Response.of(SuccessCode.OK, itemService.purchase(userDetails.getUuid()), "상품 구매 API"));
    }

    @PostMapping("/market/item/reset")
    public ResponseEntity<Response<Void>> reset() {
        itemService.reset();
        return ResponseEntity.ok(Response.of(SuccessCode.OK, null, "초기화 API"));
    }
}
