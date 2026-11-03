package com.kb.cosmetic_wms.member.application.service;

import com.kb.cosmetic_wms.member.application.port.in.BootstrapHeadquartersCommand;
import com.kb.cosmetic_wms.member.application.port.in.BootstrapHeadquartersUseCase;
import com.kb.cosmetic_wms.member.application.port.out.MemberPort;
import com.kb.cosmetic_wms.member.domain.exception.DuplicateMemberException;
import com.kb.cosmetic_wms.member.domain.model.Member;
import com.kb.cosmetic_wms.member.domain.model.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class HeadquartersBootstrapService implements BootstrapHeadquartersUseCase {

    private final MemberPort memberPort;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public boolean bootstrap(BootstrapHeadquartersCommand command) {
        if (memberPort.existsByRole(Role.ROLE_HEADQUARTERS)) {
            return false;
        }

        if (memberPort.existsByLoginId(command.loginId())) {
            throw new DuplicateMemberException();
        }

        memberPort.save(Member.create(
                command.loginId(),
                passwordEncoder.encode(command.password()),
                Role.ROLE_HEADQUARTERS,
                command.memberName(),
                command.email(),
                command.phoneNumber()
        ));
        return true;
    }
}
