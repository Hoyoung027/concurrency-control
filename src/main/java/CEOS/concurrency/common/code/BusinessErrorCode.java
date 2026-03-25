package CEOS.concurrency.common.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum BusinessErrorCode implements Code {

    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 회원입니다."),
    DUPLICATE_NAME(HttpStatus.CONFLICT, "이미 사용 중인 이름입니다."),

    ITEM_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 상품입니다."),
    OUT_OF_STOCK(HttpStatus.CONFLICT, "재고가 부족합니다."),

    STORE_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 가맹점입니다."),
    STORE_ID_MISMATCH(HttpStatus.FORBIDDEN, "잘못된 storeId입니다."),
    DUPLICATE_PAYMENT_ID(HttpStatus.CONFLICT, "이미 존재하는 결제 ID입니다."),
    PAYMENT_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "결제 처리 중 오류가 발생했습니다."),
    INVALID_API_SECRET_KEY(HttpStatus.UNAUTHORIZED, "유효하지 않은 API Secret Key입니다."),

    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "만료된 토큰입니다."),
    TOKEN_MALFORMED(HttpStatus.UNAUTHORIZED, "잘못된 형식의 토큰입니다."),
    TOKEN_INVALID(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다.");

    private final HttpStatus httpStatus;
    private final String description;

    @Override
    public int getStatusCode() {
        return httpStatus.value();
    }

    @Override
    public String getMessage() {
        return description;
    }
}
