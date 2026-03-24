package CEOS.concurrency.common.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum BusinessErrorCode implements Code {

    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 회원입니다."),
    DUPLICATE_NAME(HttpStatus.CONFLICT, "이미 사용 중인 이름입니다.");

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
