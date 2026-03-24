package CEOS.concurrency.common.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum SuccessCode implements Code {

    OK(HttpStatus.OK, "API 요청 성공"),
    GET_SUCCESS(HttpStatus.OK, "조회 성공"),
    LOGIN_SUCCESS(HttpStatus.OK, "로그인 성공"),
    UPDATE_SUCCESS(HttpStatus.OK, "업데이트 성공"),
    DELETE_SUCCESS(HttpStatus.OK, "삭제 성공"),
    CREATE_SUCCESS(HttpStatus.CREATED, "삽입 성공");

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