package com.kb.cosmetic_wms.domain.member.controller;

import com.kb.cosmetic_wms.domain.member.dto.MemberDetailResponseDto;
import com.kb.cosmetic_wms.domain.member.dto.MemberLoginRequestDto;
import com.kb.cosmetic_wms.domain.member.dto.MemberSignUpRequestDto;
import com.kb.cosmetic_wms.domain.member.service.MemberService;
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

    private final MemberService memberService;

    /**
     * WMS 사원(회원) 가입 API
     * <p>새로운 사원 정보를 입력받아 시스템에 등록하고, 생성된 회원의 상세 정보를 반환합니다.</p>
     *
     * @param requestDto 회원가입 요청 데이터 (아이디, 비밀번호, 권한, 이름, 이메일, 전화번호)
     * @return {@link ResponseEntity} 201 Created 상태 코드 및 가입된 사원의 상세 정보 DTO
     * @throws org.springframework.web.bind.MethodArgumentNotValidException 입력값 유효성 검증 실패 시 발생
     */
    @PostMapping("/signup")
    public ResponseEntity<MemberDetailResponseDto> signup(
            @Valid @RequestBody MemberSignUpRequestDto requestDto
    ) {
        MemberDetailResponseDto responseDto = memberService.register(requestDto);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(responseDto);
    }

    /**
     * WMS 사원 로그인(인증) API
     * <p>사원의 로그인 아이디와 비밀번호를 검증하여 사용자 인증을 수행하고 권한 정보를 포함한 회원 정보를 반환합니다.</p>
     *
     * @param requestDto 로그인 요청 데이터 (로그인 아이디, 비밀번호)
     * @return {@link ResponseEntity} 200 OK 상태 코드 및 인증 성공한 사원의 상세 정보 DTO
     * @throws org.springframework.web.bind.MethodArgumentNotValidException 필수 입력값 누락 시 발생
     */
    @PostMapping("/login")
    public ResponseEntity<MemberDetailResponseDto> login(
            @Valid @RequestBody MemberLoginRequestDto requestDto
    ) {
        MemberDetailResponseDto responseDto = memberService.login(requestDto);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(responseDto);
    }
}
