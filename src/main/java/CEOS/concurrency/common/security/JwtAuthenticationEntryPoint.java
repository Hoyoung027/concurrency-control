package CEOS.concurrency.common.security;

import CEOS.concurrency.common.code.BusinessErrorCode;
import CEOS.concurrency.common.code.GeneralErrorCode;
import CEOS.concurrency.common.exception.BusinessException;
import CEOS.concurrency.common.jwt.JwtFilter;
import CEOS.concurrency.common.response.Response;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        BusinessException jwtException =
                (BusinessException) request.getAttribute(JwtFilter.JWT_EXCEPTION_ATTRIBUTE);

        Object body;
        if (jwtException != null) {
            BusinessErrorCode errorCode = jwtException.getBusinessErrorCode();
            response.setStatus(errorCode.getStatusCode());
            body = Response.error(errorCode, request.getRequestURI());
        } else {
            GeneralErrorCode errorCode = GeneralErrorCode.UNAUTHORIZED;
            response.setStatus(errorCode.getStatusCode());
            body = Response.error(errorCode, request.getRequestURI());
        }

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }
}
