package com.kb.ordering.member.domain.model;


import com.kb.ordering.member.domain.exception.MemberValidationException;
import com.kb.ordering.member.domain.valueobject.Email;
import com.kb.ordering.member.domain.valueobject.PhoneNumber;
import lombok.Getter;

@Getter
public class Member {

    private final Long memberId;
    private final Role role;
    private final String memberName;
    private final Email email;
    private final PhoneNumber phoneNumber;

    private Member(Long memberId, Role role, String memberName, Email email, PhoneNumber phoneNumber) {
        this.memberId = memberId;
        this.role = role;
        this.memberName = memberName;
        this.email = email;
        this.phoneNumber = phoneNumber;
    }

    public static Member create(Role role, String memberName, String email, String phoneNumber) {
        validateMemberName(memberName);
        return new Member(
                null,
                role,
                memberName,
                new Email(email),
                new PhoneNumber(phoneNumber)
        );
    }

    public static Member reconstitute(Long memberId, Role role, String memberName,
                                      String email, String phoneNumber) {
        return new Member(
                memberId,
                role,
                memberName,
                new Email(email),
                new PhoneNumber(phoneNumber)
        );
    }

    private static void validateMemberName(String memberName) {
        if (memberName == null || memberName.isBlank()) {
            throw new MemberValidationException();
        }
    }
}
