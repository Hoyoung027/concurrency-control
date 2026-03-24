package CEOS.concurrency.common.exception;

import CEOS.concurrency.common.code.BusinessErrorCode;
import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {

    private final BusinessErrorCode businessErrorCode;

    public BusinessException(BusinessErrorCode businessErrorCode) {
        super(businessErrorCode.getMessage());
        this.businessErrorCode = businessErrorCode;
    }

    public BusinessException(BusinessErrorCode businessErrorCode, String message) {
        super(message);
        this.businessErrorCode = businessErrorCode;
    }
}