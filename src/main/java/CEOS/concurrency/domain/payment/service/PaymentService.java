package CEOS.concurrency.domain.payment.service;

import CEOS.concurrency.common.code.BusinessErrorCode;
import CEOS.concurrency.common.exception.BusinessException;
import CEOS.concurrency.common.jwt.JwtProvider;
import CEOS.concurrency.domain.payment.dto.InstantPaymentRequest;
import CEOS.concurrency.domain.payment.dto.PaymentResponse;
import CEOS.concurrency.domain.payment.dto.StoreResponse;
import CEOS.concurrency.domain.payment.entity.PaymentLog;
import CEOS.concurrency.domain.payment.entity.Store;
import CEOS.concurrency.domain.payment.enums.PaymentStatus;
import CEOS.concurrency.domain.payment.repository.PaymentLogRepository;
import CEOS.concurrency.domain.payment.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.random.RandomGenerator;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final StoreRepository storeRepository;
    private final PaymentLogRepository paymentLogRepository;
    private final PaymentLogWriter paymentLogWriter;
    private final JwtProvider jwtProvider;

    private static final RandomGenerator RANDOM = RandomGenerator.getDefault();

    @Transactional
    public StoreResponse getOrCreateStore(String githubId) {
        Store store = storeRepository.findByGithubId(githubId)
                .orElseGet(() -> storeRepository.save(
                        Store.builder()
                                .githubId(githubId)
                                .apiSecretKey(jwtProvider.generateApiSecretKey(githubId))
                                .build()
                ));
        return StoreResponse.from(store);
    }

    @Transactional
    public PaymentResponse requestPayment(String githubId, String paymentId, InstantPaymentRequest request) {

        Store storeByToken = storeRepository.findByGithubId(githubId)
                .orElseThrow(() -> new BusinessException(BusinessErrorCode.STORE_NOT_FOUND));

        Store storeByStoreId = storeRepository.findByGithubId(request.storeId())
                .orElseThrow(() -> new BusinessException(BusinessErrorCode.STORE_NOT_FOUND));

        if (!storeByToken.getId().equals(storeByStoreId.getId())) {
            throw new BusinessException(BusinessErrorCode.STORE_ID_MISMATCH);
        }

        if (paymentLogRepository.existsByStore_IdAndPaymentId(storeByToken.getId(), paymentId)) {
            throw new BusinessException(BusinessErrorCode.DUPLICATE_PAYMENT_ID);
        }

        boolean isFailed = RANDOM.nextDouble() < 0.1;
        PaymentStatus status = isFailed ? PaymentStatus.FAILED : PaymentStatus.PAID;

        PaymentResponse response = paymentLogWriter.save(
                paymentId, storeByToken,
                request.orderName(),
                request.totalPayAmount().longValue(),
                request.currency(),
                request.customData(),
                status
        );

        if (isFailed) {
            log.warn("Payment failed by random failure policy. paymentId={}, storeId={}", paymentId, githubId);
            throw new BusinessException(BusinessErrorCode.PAYMENT_FAILED);
        }

        return response;
    }

    @Transactional
    public PaymentResponse cancelPayment(String githubId, String paymentId) {

        Store store = storeRepository.findByGithubId(githubId)
                .orElseThrow(() -> new BusinessException(BusinessErrorCode.STORE_NOT_FOUND));

        PaymentLog paymentLog = paymentLogRepository.findByStore_IdAndPaymentId(store.getId(), paymentId)
                .orElseThrow(() -> new BusinessException(BusinessErrorCode.PAYMENT_NOT_FOUND));

        if (paymentLog.getStatus() != PaymentStatus.PAID) {
            throw new BusinessException(BusinessErrorCode.PAYMENT_NOT_CANCELLABLE);
        }

        paymentLog.cancel();
        return PaymentResponse.from(paymentLog);
    }

    @Transactional
    public PaymentResponse getPayment(String githubId, String paymentId) {

        Store store = storeRepository.findByGithubId(githubId)
                .orElseThrow(() -> new BusinessException(BusinessErrorCode.STORE_NOT_FOUND));

        PaymentLog paymentLog = paymentLogRepository.findByStore_IdAndPaymentId(store.getId(), paymentId)
                .orElseThrow(() -> new BusinessException(BusinessErrorCode.PAYMENT_NOT_FOUND));

        return PaymentResponse.from(paymentLog);
    }
}
