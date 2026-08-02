package com.kb.auth.auth.application.service;

import com.kb.auth.auth.application.port.in.LoginUseCase;
import com.kb.auth.auth.application.port.in.LogoutUseCase;
import com.kb.auth.auth.application.port.in.ReissueTokenUseCase;
import com.kb.auth.auth.application.port.in.SignUpUseCase;
import com.kb.auth.auth.application.port.in.dto.LoginCommand;
import com.kb.auth.auth.application.port.in.dto.LoginResult;
import com.kb.auth.auth.application.port.in.dto.LogoutCommand;
import com.kb.auth.auth.application.port.in.dto.ReissueCommand;
import com.kb.auth.auth.application.port.in.dto.ReissueResult;
import com.kb.auth.auth.application.port.in.dto.SignUpCommand;
import com.kb.auth.auth.application.port.in.dto.SignUpResult;
import com.kb.auth.auth.application.port.out.CredentialPort;
import com.kb.auth.auth.application.port.out.RefreshTokenPort;
import com.kb.auth.auth.application.port.out.TokenIssuer;
import com.kb.auth.auth.application.port.out.TokenParser;
import com.kb.auth.auth.domain.exception.DuplicateLoginIdException;
import com.kb.auth.auth.domain.exception.HeadquartersRoleNotAllowedException;
import com.kb.auth.auth.domain.exception.InvalidRefreshTokenException;
import com.kb.auth.auth.domain.exception.LoginFailedException;
import com.kb.auth.auth.domain.model.Credential;
import com.kb.auth.member.application.port.in.FindMemberUseCase;
import com.kb.auth.member.application.port.in.RegisterMemberUseCase;
import com.kb.auth.member.application.port.in.dto.MemberResult;
import com.kb.auth.member.application.port.in.dto.RegisterMemberCommand;
import com.kb.auth.member.domain.model.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService implements LoginUseCase, SignUpUseCase, ReissueTokenUseCase, LogoutUseCase {

    private final CredentialPort credentialPort;
    private final FindMemberUseCase findMemberUseCase;
    private final RegisterMemberUseCase registerMemberUseCase;
    private final PasswordEncoder passwordEncoder;
    private final TokenIssuer tokenIssuer;
    private final TokenParser tokenParser;
    private final RefreshTokenPort refreshTokenPort;

    @Override
    @Transactional
    public LoginResult login(LoginCommand command) {
        Credential credential = credentialPort.findByLoginId(command.loginId())
                .orElseThrow(LoginFailedException::new);

        if (!passwordEncoder.matches(command.password(), credential.getEncodedPassword())) {
            throw new LoginFailedException();
        }

        MemberResult member = findMemberUseCase.findById(credential.getMemberId());
        String accessToken = tokenIssuer.issueAccessToken(member.memberId(), member.role());
        String refreshToken = tokenIssuer.issueRefreshToken(member.memberId());
        refreshTokenPort.save(member.memberId(), refreshToken);

        return new LoginResult(member.memberId(), member.memberName(), member.role(), accessToken, refreshToken);
    }

    @Override
    @Transactional
    public void logout(LogoutCommand command) {
        Long memberId = tokenParser.extractMemberId(command.refreshToken())
                .orElseThrow(InvalidRefreshTokenException::new);

        String storedToken = refreshTokenPort.find(memberId)
                .orElseThrow(InvalidRefreshTokenException::new);

        if (!storedToken.equals(command.refreshToken())) {
            throw new InvalidRefreshTokenException();
        }

        refreshTokenPort.delete(memberId);
    }

    @Override
    @Transactional
    public ReissueResult reissue(ReissueCommand command) {
        Long memberId = tokenParser.extractMemberId(command.refreshToken())
                .orElseThrow(InvalidRefreshTokenException::new);

        String storedToken = refreshTokenPort.find(memberId)
                .orElseThrow(InvalidRefreshTokenException::new);

        if (!storedToken.equals(command.refreshToken())) {
            throw new InvalidRefreshTokenException();
        }

        MemberResult member = findMemberUseCase.findById(memberId);
        String newAccessToken = tokenIssuer.issueAccessToken(member.memberId(), member.role());
        String newRefreshToken = tokenIssuer.issueRefreshToken(member.memberId());
        refreshTokenPort.save(memberId, newRefreshToken);

        return new ReissueResult(memberId, newAccessToken, newRefreshToken);
    }

    @Override
    @Transactional
    public SignUpResult signUp(SignUpCommand command) {
        if (command.role() == Role.ROLE_HEADQUARTERS) {
            throw new HeadquartersRoleNotAllowedException();
        }

        if (credentialPort.existsByLoginId(command.loginId())) {
            throw new DuplicateLoginIdException();
        }

        MemberResult member = registerMemberUseCase.register(new RegisterMemberCommand(
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
