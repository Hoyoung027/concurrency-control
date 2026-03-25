package CEOS.concurrency.domain.payment.controller;

import CEOS.concurrency.common.code.SuccessCode;
import CEOS.concurrency.common.response.Response;
import CEOS.concurrency.domain.payment.dto.InstantPaymentRequest;
import CEOS.concurrency.domain.payment.dto.PaymentResponse;
import CEOS.concurrency.domain.payment.dto.StoreResponse;
import CEOS.concurrency.domain.payment.security.StoreUserDetails;
import CEOS.concurrency.domain.payment.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @GetMapping("/auth/{githubId}")
    public ResponseEntity<Response<StoreResponse>> getStore(@PathVariable String githubId) {
        return ResponseEntity.ok(Response.of(SuccessCode.GET_SUCCESS, paymentService.getOrCreateStore(githubId), "결제용 API Secret 조회"));
    }

    @PreAuthorize("hasRole('STORE')")
    @PostMapping("/payments/{paymentId}/instant")
    public ResponseEntity<Response<PaymentResponse>> requestPayment(
            @AuthenticationPrincipal StoreUserDetails storeUserDetails,
            @PathVariable String paymentId,
            @Valid @RequestBody InstantPaymentRequest request) {
        return ResponseEntity.ok(Response.of(SuccessCode.CREATE_SUCCESS,
                paymentService.requestPayment(storeUserDetails.getGithubId(), paymentId, request), "결제 처리 완료"));
    }

    @PreAuthorize("hasRole('STORE')")
    @PostMapping("/payments/{paymentId}/cancel")
    public ResponseEntity<Response<PaymentResponse>> cancelPayment(
            @AuthenticationPrincipal StoreUserDetails storeUserDetails,
            @PathVariable String paymentId) {
        return ResponseEntity.ok(Response.of(SuccessCode.DELETE_SUCCESS,
                paymentService.cancelPayment(storeUserDetails.getGithubId(), paymentId), "결제 취소 완료"));
    }

    @PreAuthorize("hasRole('STORE')")
    @GetMapping("/payments/{paymentId}")
    public ResponseEntity<Response<PaymentResponse>> getPayment(
            @AuthenticationPrincipal StoreUserDetails storeUserDetails,
            @PathVariable String paymentId) {
        return ResponseEntity.ok(Response.of(SuccessCode.GET_SUCCESS,
                paymentService.getPayment(storeUserDetails.getGithubId(), paymentId), "결제 내역 조회"));
    }
}
