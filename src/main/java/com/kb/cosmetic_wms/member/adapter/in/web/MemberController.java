package com.kb.cosmetic_wms.member.adapter.in.web;

import com.kb.cosmetic_wms.member.application.port.in.LoginMemberUseCase;
import com.kb.cosmetic_wms.member.application.port.in.MemberResult;
import com.kb.cosmetic_wms.member.application.port.in.RegisterMemberUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/members")
@RequiredArgsConstructor
public class MemberController {

    private final RegisterMemberUseCase registerMemberUseCase;
    private final LoginMemberUseCase loginMemberUseCase;

    @PostMapping("/signup")
    public ResponseEntity<MemberResponse> signup(
            @Valid @RequestBody MemberSignUpRequest request
    ) {
        MemberResult result = registerMemberUseCase.register(request.toCommand());
        return ResponseEntity.status(HttpStatus.CREATED).body(MemberResponse.from(result));
    }

    @PostMapping("/login")
    public ResponseEntity<MemberResponse> login(
            @Valid @RequestBody MemberLoginRequest request
    ) {
        MemberResult result = loginMemberUseCase.login(request.toCommand());
        return ResponseEntity.ok(MemberResponse.from(result));
    }
}