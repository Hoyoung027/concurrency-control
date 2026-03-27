package CEOS.concurrency.domain.member.service;

import CEOS.concurrency.common.code.BusinessErrorCode;
import CEOS.concurrency.common.exception.BusinessException;
import CEOS.concurrency.common.jwt.JwtProvider;
import CEOS.concurrency.domain.member.dto.LoginRequest;
import CEOS.concurrency.domain.member.dto.LoginResponse;
import CEOS.concurrency.domain.member.dto.SignupRequest;
import CEOS.concurrency.domain.member.dto.SignupResponse;
import CEOS.concurrency.domain.member.entity.Member;
import CEOS.concurrency.domain.member.repository.MemberRepository;
import CEOS.concurrency.domain.member.security.CustomUserDetails;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtProvider jwtProvider;

    @Value("${jwt.access-expiration}")
    private long accessExpiration;

    @Value("${jwt.refresh-expiration}")
    private long refreshExpiration;

    @Value("${cookie.secure}")
    private boolean cookieSecure;

    @Transactional
    public SignupResponse signup(SignupRequest request, HttpServletResponse response) {
        if (memberRepository.findByNickname(request.nickname()).isPresent()) {
            throw new BusinessException(BusinessErrorCode.DUPLICATE_NAME);
        }

        Member member = Member.builder()
                .nickname(request.nickname())
                .password(passwordEncoder.encode(request.password()))
                .characterType(request.characterType())
                .build();

        memberRepository.save(member);
        setTokenCookies(member.getUuid(), response);
        return SignupResponse.from(member);
    }

    public LoginResponse login(LoginRequest request, HttpServletResponse response) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.nickname(), request.password())
        );

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        setTokenCookies(userDetails.getUuid(), response);
        return new LoginResponse(userDetails.getNickname(), userDetails.getCharacterType().getEmoji());
    }

    private void setTokenCookies(UUID uuid, HttpServletResponse response) {
        String accessToken = jwtProvider.generateAccessToken(uuid);
        String refreshToken = jwtProvider.generateRefreshToken(uuid);

        ResponseCookie accessCookie = ResponseCookie.from("access_token", accessToken)
                .httpOnly(true).secure(cookieSecure).path("/").maxAge(accessExpiration / 1000).sameSite("Lax").build();
        ResponseCookie refreshCookie = ResponseCookie.from("refresh_token", refreshToken)
                .httpOnly(true).secure(cookieSecure).path("/").maxAge(refreshExpiration / 1000).sameSite("Lax").build();

        response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());
    }
}
