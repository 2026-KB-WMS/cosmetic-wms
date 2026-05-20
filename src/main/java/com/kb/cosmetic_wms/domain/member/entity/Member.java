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
        validatePassword(request.password());

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
            throw new IllegalArgumentException(MemberConstants.INVALID_EMAIL_MESSAGE);
        }
    }

    private static void validatePhoneNumber(String phoneNumber) {
        String phoneNumberRegex = MemberConstants.PHONE_NUMBER_REGEX;
        if (phoneNumber == null || !Pattern.matches(phoneNumberRegex, phoneNumber)) {
            throw new IllegalArgumentException(MemberConstants.INVALID_PHONE_NUMBER_MESSAGE);
        }
    }

    private static void validateLoginId(String loginId) {
        if (loginId == null || loginId.isBlank()) {
            throw new IllegalArgumentException(MemberConstants.LOGIN_ID_REQUIRED_MESSAGE);
        }
        if (loginId.length() < MemberConstants.LOGIN_ID_MIN_LENGTH ||
                loginId.length() > MemberConstants.LOGIN_ID_MAX_LENGTH) {
            throw new IllegalArgumentException(MemberConstants.INVALID_LOGIN_ID_LENGTH_MESSAGE);
        }
    }

    private static void validatePassword(String password) {
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException(MemberConstants.PASSWORD_REQUIRED_MESSAGE);
        }
        if (password.length() < MemberConstants.PASSWORD_MIN_LENGTH ||
                password.length() > MemberConstants.PASSWORD_MAX_LENGTH) {
            throw new IllegalArgumentException(MemberConstants.INVALID_PASSWORD_LENGTH_MESSAGE);
        }
        if (!Pattern.matches(MemberConstants.PASSWORD_REGEX, password)) {
            throw new IllegalArgumentException(MemberConstants.INVALID_PASSWORD_MESSAGE);
        }
    }
}
