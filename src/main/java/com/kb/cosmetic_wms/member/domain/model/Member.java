package com.kb.cosmetic_wms.member.domain.model;

import com.kb.cosmetic_wms.member.domain.exception.MemberValidationException;
import com.kb.cosmetic_wms.member.domain.valueobject.Email;
import com.kb.cosmetic_wms.member.domain.valueobject.LoginId;
import com.kb.cosmetic_wms.member.domain.valueobject.PhoneNumber;
import lombok.Getter;

@Getter
public class Member {

    private final Long memberId;
    private final LoginId loginId;
    private final String encodedPassword;
    private final Role role;
    private final String memberName;
    private final Email email;
    private final PhoneNumber phoneNumber;

    private Member(Long memberId, LoginId loginId, String encodedPassword, Role role,
                   String memberName, Email email, PhoneNumber phoneNumber) {
        this.memberId = memberId;
        this.loginId = loginId;
        this.encodedPassword = encodedPassword;
        this.role = role;
        this.memberName = memberName;
        this.email = email;
        this.phoneNumber = phoneNumber;
    }

    public static Member create(String loginId, String encodedPassword, Role role,
                                String memberName, String email, String phoneNumber) {
        validateEncodedPassword(encodedPassword);
        return new Member(
                null,
                new LoginId(loginId),
                encodedPassword,
                role,
                memberName,
                new Email(email),
                new PhoneNumber(phoneNumber)
        );
    }

    public static Member reconstitute(Long memberId, String loginId, String encodedPassword, Role role,
                                      String memberName, String email, String phoneNumber) {
        return new Member(
                memberId,
                new LoginId(loginId),
                encodedPassword,
                role,
                memberName,
                new Email(email),
                new PhoneNumber(phoneNumber)
        );
    }

    private static void validateEncodedPassword(String encodedPassword) {
        if (encodedPassword == null || encodedPassword.isBlank()) {
            throw new MemberValidationException();
        }
    }
}