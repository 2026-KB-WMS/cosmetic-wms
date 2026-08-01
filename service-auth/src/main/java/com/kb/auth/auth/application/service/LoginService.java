package com.kb.auth.auth.application.service;

import com.kb.auth.auth.application.port.in.LoginUseCase;
import com.kb.auth.auth.application.port.in.dto.LoginCommand;
import com.kb.auth.auth.application.port.in.dto.LoginResult;
import com.kb.auth.auth.application.port.out.CredentialPort;
import com.kb.auth.auth.application.port.out.LoadMemberPort;
import com.kb.auth.auth.application.port.out.TokenProvider;
import com.kb.auth.auth.application.port.out.dto.MemberInfo;
import com.kb.auth.auth.domain.exception.LoginFailedException;
import com.kb.auth.auth.domain.model.Credential;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class LoginService implements LoginUseCase {

    private final CredentialPort credentialPort;
    private final LoadMemberPort loadMemberPort;
    private final PasswordEncoder passwordEncoder;
    private final TokenProvider tokenProvider;

    @Override
    public LoginResult login(LoginCommand command) {
        Credential credential = credentialPort.findByLoginId(command.loginId())
                .orElseThrow(LoginFailedException::new);

        if (!passwordEncoder.matches(command.password(), credential.getEncodedPassword())) {
            throw new LoginFailedException();
        }

        MemberInfo member = loadMemberPort.loadById(credential.getMemberId());
        String accessToken = tokenProvider.issue(member.memberId(), member.role());

        return new LoginResult(member.memberId(), member.memberName(), member.role(), accessToken);
    }
}
