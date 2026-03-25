package CEOS.concurrency.domain.member.controller;

import CEOS.concurrency.common.code.SuccessCode;
import CEOS.concurrency.common.response.Response;
import CEOS.concurrency.domain.member.dto.LoginRequest;
import CEOS.concurrency.domain.member.dto.LoginResponse;
import CEOS.concurrency.domain.member.dto.SignupRequest;
import CEOS.concurrency.domain.member.dto.SignupResponse;
import CEOS.concurrency.domain.member.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class MemberController {

    private final MemberService memberService;

    @PostMapping("/auth/signup")
    public ResponseEntity<Response<SignupResponse>> signup(@RequestBody @Valid SignupRequest request) {
        return ResponseEntity.ok(Response.of(SuccessCode.CREATE_SUCCESS, memberService.signup(request), "회원 가입 API"));
    }

    @PostMapping("/auth/login")
    public ResponseEntity<Response<LoginResponse>> login(@RequestBody @Valid LoginRequest request) {
        return ResponseEntity.ok(Response.of(SuccessCode.LOGIN_SUCCESS, memberService.login(request), "로그인 API"));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/auth/test")
    public ResponseEntity<Response<LoginResponse>> test() {
        return ResponseEntity.ok(Response.of(SuccessCode.OK, null, "테스트 API"));
    }
}
