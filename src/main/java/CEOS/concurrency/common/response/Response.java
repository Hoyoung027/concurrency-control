package CEOS.concurrency.common.response;

import CEOS.concurrency.common.code.Code;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.springframework.http.HttpStatus;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"status", "error", "message", "payload", "path", "timestamp"})
public record Response<T>(
        String timestamp,
        int status,
        String error,
        String message,
        String path,
        T payload
) {

    // 성공 응답 - timestamp, error, path 미포함
    public static <T> Response<T> of(Code code, T payload, String message) {
        return new Response<>(null, code.getStatusCode(), null, message, null, payload);
    }

    // 에러 응답 - timestamp, error, path 포함
    public static Response<Void> error(Code code, String path) {
        return new Response<>(now(), code.getStatusCode(), reasonPhrase(code), code.getMessage(), path, null);
    }

    public static <T> Response<T> error(Code code, T payload, String path) {
        return new Response<>(now(), code.getStatusCode(), reasonPhrase(code), code.getMessage(), path, payload);
    }

    private static String reasonPhrase(Code code) {
        return HttpStatus.valueOf(code.getStatusCode()).getReasonPhrase();
    }

    private static String now() {
        return OffsetDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX"));
    }
}
