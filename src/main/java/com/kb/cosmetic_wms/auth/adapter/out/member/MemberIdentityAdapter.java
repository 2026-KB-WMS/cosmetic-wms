package com.kb.cosmetic_wms.auth.adapter.out.member;

import com.kb.cosmetic_wms.auth.application.port.out.MemberIdentity;
import com.kb.cosmetic_wms.auth.application.port.out.MemberIdentityPort;
import com.kb.cosmetic_wms.member.application.port.in.LoginMemberCommand;
import com.kb.cosmetic_wms.member.application.port.in.MemberIdentityResult;
import com.kb.cosmetic_wms.member.application.port.in.MemberIdentityUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class MemberIdentityAdapter implements MemberIdentityPort {

    private final MemberIdentityUseCase memberIdentityUseCase;

    @Override
    public MemberIdentity authenticate(String loginId, String password) {
        return toIdentity(memberIdentityUseCase.authenticate(new LoginMemberCommand(loginId, password)));
    }

    @Override
    public Optional<MemberIdentity> findById(Long memberId) {
        return memberIdentityUseCase.findIdentity(memberId).map(MemberIdentityAdapter::toIdentity);
    }

    private static MemberIdentity toIdentity(MemberIdentityResult result) {
        return new MemberIdentity(result.memberId(), result.role());
    }
}
