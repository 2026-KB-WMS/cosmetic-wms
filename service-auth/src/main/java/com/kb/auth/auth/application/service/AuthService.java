package com.kb.auth.auth.application.service;

import com.kb.auth.auth.application.port.in.LoginUseCase;
import com.kb.auth.auth.application.port.in.SignUpUseCase;
import com.kb.auth.auth.application.port.in.dto.LoginCommand;
import com.kb.auth.auth.application.port.in.dto.LoginResult;
import com.kb.auth.auth.application.port.in.dto.SignUpCommand;
import com.kb.auth.auth.application.port.in.dto.SignUpResult;
import com.kb.auth.auth.application.port.out.CredentialPort;
import com.kb.auth.auth.application.port.out.MemberPort;
import com.kb.auth.auth.application.port.out.RefreshTokenPort;
import com.kb.auth.auth.application.port.out.TokenIssuer;
import com.kb.auth.auth.application.port.out.dto.MemberInfo;
import com.kb.auth.auth.application.port.out.dto.MemberRegistration;
import com.kb.auth.auth.domain.exception.DuplicateLoginIdException;
import com.kb.auth.auth.domain.exception.LoginFailedException;
import com.kb.auth.auth.domain.model.Credential;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService implements LoginUseCase, SignUpUseCase {

    private final CredentialPort credentialPort;
    private final MemberPort memberPort;
    private final PasswordEncoder passwordEncoder;
    private final TokenIssuer tokenIssuer;
    private final RefreshTokenPort refreshTokenPort;

    @Override
    public LoginResult login(LoginCommand command) {
        Credential credential = credentialPort.findByLoginId(command.loginId())
                .orElseThrow(LoginFailedException::new);

        if (!passwordEncoder.matches(command.password(), credential.getEncodedPassword())) {
            throw new LoginFailedException();
        }

        MemberInfo member = memberPort.loadById(credential.getMemberId());
        String accessToken = tokenIssuer.issueAccessToken(member.memberId(), member.role());
        String refreshToken = tokenIssuer.issueRefreshToken(member.memberId());
        refreshTokenPort.save(member.memberId(), refreshToken);

        return new LoginResult(member.memberId(), member.memberName(), member.role(), accessToken, refreshToken);
    }

    @Override
    @Transactional
    public SignUpResult signUp(SignUpCommand command) {
        if (credentialPort.existsByLoginId(command.loginId())) {
            throw new DuplicateLoginIdException();
        }

        MemberInfo member = memberPort.register(new MemberRegistration(
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
