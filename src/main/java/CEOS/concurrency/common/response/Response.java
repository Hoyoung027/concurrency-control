package CEOS.concurrency.common.response;

import CEOS.concurrency.common.code.Code;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record Response<T>(
        int status,
        String description,
        T payload
) {

    public static Response<Void> of(Code code) {
        return new Response<>(code.getStatusCode(), code.getMessage(), null);
    }

    public static <T> Response<T> of(Code code, T payload) {
        return new Response<>(code.getStatusCode(), code.getMessage(), payload);
    }

    public static <T> Response<T> of(Code code, T payload, String message) {
        return new Response<>(code.getStatusCode(), message, payload);
    }
}