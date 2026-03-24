package CEOS.concurrency.common.jwt;

import CEOS.concurrency.common.code.BusinessErrorCode;
import CEOS.concurrency.common.exception.BusinessException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

@Component
public class JwtProvider {

    private final SecretKey secretKey;
    private final long accessExpiration;
    private final long refreshExpiration;

    public JwtProvider(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-expiration}") long accessExpiration,
            @Value("${jwt.refresh-expiration}") long refreshExpiration
    ) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessExpiration = accessExpiration;
        this.refreshExpiration = refreshExpiration;
    }

    public String generateAccessToken(UUID uuid) {
        return buildToken(uuid, accessExpiration);
    }

    public String generateRefreshToken(UUID uuid) {
        return buildToken(uuid, refreshExpiration);
    }

    public UUID validateAndExtractUuid(String token) {
        try {
            return UUID.fromString(parseClaims(token).getSubject());
        } catch (ExpiredJwtException e) {
            throw new BusinessException(BusinessErrorCode.TOKEN_EXPIRED);
        } catch (MalformedJwtException | UnsupportedJwtException | SignatureException e) {
            throw new BusinessException(BusinessErrorCode.TOKEN_MALFORMED);
        } catch (JwtException | IllegalArgumentException e) {
            throw new BusinessException(BusinessErrorCode.TOKEN_INVALID);
        }
    }

    private String buildToken(UUID uuid, long expiration) {
        Date now = new Date();
        return Jwts.builder()
                .subject(uuid.toString())
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expiration))
                .signWith(secretKey)
                .compact();
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
