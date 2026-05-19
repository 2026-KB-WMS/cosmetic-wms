package com.kb.cosmetic_wms.domain.member.entity;

import com.kb.cosmetic_wms.domain.member.MemberConstants;
import com.kb.cosmetic_wms.domain.member.dto.MemberSignUpRequest;
import com.kb.cosmetic_wms.domain.member.enums.Role;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.regex.Pattern;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Member {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String loginId;
    private String password;

    @Enumerated(EnumType.STRING)
    private Role role;

    private String memberName;
    private String email;
    private String phoneNumber;

    private Member(String loginId, String password, Role role, String memberName, String email, String phoneNumber) {
        this.loginId = loginId;
        this.password = password;
        this.role = role;
        this.memberName = memberName;
        this.email = email;
        this.phoneNumber = phoneNumber;
    }

    public static Member create(MemberSignUpRequest request) {
        validateEmail(request.email());
        validatePhoneNumber(request.phoneNumber());
        validateLoginId(request.loginId());

        return new Member(
                request.loginId(),
                request.password(),
                request.role(),
                request.memberName(),
                request.email(),
                request.phoneNumber()
        );
    }

    private static void validateEmail(String email) {
        String emailRegex = MemberConstants.EMAIL_REGEX;
        if (email == null || !Pattern.matches(emailRegex, email)) {
            throw new IllegalArgumentException("올바르지 않은 이메일 형식입니다.");
        }
    }

    private static void validatePhoneNumber(String phoneNumber) {
        String phoneNumberRegex = MemberConstants.PHONE_NUMBER_REGEX;
        if (phoneNumber == null || !Pattern.matches(phoneNumberRegex, phoneNumber)) {
            throw new IllegalArgumentException("올바르지 않은 전화번호 형식입니다. (예: 010-1234-5678)");
        }
    }

    private static void validateLoginId(String loginId) {
        if (loginId == null || loginId.trim().isEmpty()) {
            throw new IllegalArgumentException("로그인 ID는 필수 입력 항목입니다.");
        }
        if (loginId.length() > MemberConstants.LOGIN_ID_MAX_LENGTH) {
            throw new IllegalArgumentException("로그인 ID는 50자를 초과할 수 없습니다.");
        }
        if (loginId.length() < MemberConstants.LOGIN_ID_MIN_LENGTH) {
            throw new IllegalArgumentException("로그인 ID는 5자 이상이어야 합니다.");
        }
    }
}
