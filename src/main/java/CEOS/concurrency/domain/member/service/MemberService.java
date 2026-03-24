package CEOS.concurrency.domain.member.service;

import CEOS.concurrency.common.jwt.JwtProvider;
import CEOS.concurrency.domain.member.dto.LoginRequest;
import CEOS.concurrency.domain.member.dto.LoginResponse;
import CEOS.concurrency.domain.member.dto.SignupRequest;
import CEOS.concurrency.domain.member.dto.SignupResponse;
import CEOS.concurrency.domain.member.entity.Member;
import CEOS.concurrency.domain.member.repository.MemberRepository;
import CEOS.concurrency.domain.member.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtProvider jwtProvider;

    @Transactional
    public SignupResponse signup(SignupRequest request) {
        Member member = Member.builder()
                .nickname(request.nickname())
                .password(passwordEncoder.encode(request.password()))
                .characterType(request.characterType())
                .build();

        memberRepository.save(member);
        return SignupResponse.from(member);
    }

    public LoginResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.nickname(), request.password())
        );

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        String accessToken = jwtProvider.generateAccessToken(userDetails.getUuid());
        String refreshToken = jwtProvider.generateRefreshToken(userDetails.getUuid());

        return new LoginResponse(accessToken, refreshToken);
    }
}
