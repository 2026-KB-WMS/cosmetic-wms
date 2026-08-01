package com.kb.auth.auth.application.service;

import com.kb.auth.auth.application.port.in.SignUpUseCase;
import com.kb.auth.auth.application.port.in.dto.SignUpCommand;
import com.kb.auth.auth.application.port.in.dto.SignUpResult;
import com.kb.auth.auth.application.port.out.CredentialPort;
import com.kb.auth.auth.application.port.out.RegisterMemberPort;
import com.kb.auth.auth.application.port.out.dto.MemberInfo;
import com.kb.auth.auth.application.port.out.dto.MemberRegistration;
import com.kb.auth.auth.domain.exception.DuplicateLoginIdException;
import com.kb.auth.auth.domain.model.Credential;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SignUpService implements SignUpUseCase {

    private final CredentialPort credentialPort;
    private final RegisterMemberPort registerMemberPort;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public SignUpResult signUp(SignUpCommand command) {
        if (credentialPort.existsByLoginId(command.loginId())) {
            throw new DuplicateLoginIdException();
        }

        MemberInfo member = registerMemberPort.register(new MemberRegistration(
                command.role(),
                command.memberName(),
                command.email(),
                command.phoneNumber()
        ));

        Credential credential = Credential.create(
                member.memberId(),
                command.loginId(),
                passwordEncoder.encode(command.password())
        );
        credentialPort.save(credential);

        return SignUpResult.of(member, command.loginId());
    }
}
