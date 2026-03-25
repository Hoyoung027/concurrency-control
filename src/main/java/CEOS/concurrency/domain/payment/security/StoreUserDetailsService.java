package CEOS.concurrency.domain.payment.security;

import CEOS.concurrency.common.code.BusinessErrorCode;
import CEOS.concurrency.common.exception.BusinessException;
import CEOS.concurrency.domain.payment.entity.Store;
import CEOS.concurrency.domain.payment.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StoreUserDetailsService {

    private final StoreRepository storeRepository;

    public StoreUserDetails loadByGithubId(String githubId) {
        Store store = storeRepository.findByGithubId(githubId)
                .orElseThrow(() -> new BusinessException(BusinessErrorCode.STORE_NOT_FOUND));
        return new StoreUserDetails(store);
    }
}
