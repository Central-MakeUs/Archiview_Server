package zero.conflict.archiview.user.application.command;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import zero.conflict.archiview.auth.domain.CustomOAuth2User;
import zero.conflict.archiview.auth.infrastructure.JwtTokenProvider;
import zero.conflict.archiview.global.error.DomainException;
import zero.conflict.archiview.user.application.port.out.EditorProfileRepository;
import zero.conflict.archiview.user.application.port.out.UserRepository;
import zero.conflict.archiview.user.domain.User;
import zero.conflict.archiview.user.domain.error.UserErrorCode;
import zero.conflict.archiview.user.dto.UserDto;
import zero.conflict.archiview.user.application.support.NicknameGenerator;
import zero.conflict.archiview.user.application.port.out.ArchiverProfileRepository;
import zero.conflict.archiview.user.domain.ArchiverProfile;

@Service
@RequiredArgsConstructor
public class UserCommandService {

    private final UserRepository userRepository;
    private final NicknameGenerator nicknameGenerator;
    private final ArchiverProfileRepository archiverProfileRepository;
    private final EditorProfileRepository editorProfileRepository;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public void completeOnboarding(java.util.UUID userId, UserDto.OnboardingRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new DomainException(UserErrorCode.USER_NOT_FOUND));

        if (user.getRole() != User.Role.GUEST) {
            throw new DomainException(UserErrorCode.ALREADY_ONBOARDED);
        }

        user.assignRole(request.getRole());

        if (user.getRole() == User.Role.ARCHIVER) {
            String nickname = nicknameGenerator.generate();
            ArchiverProfile profile = ArchiverProfile.createOf(user, nickname);
            archiverProfileRepository.save(profile);
        }

        userRepository.save(user);
    }

    @Transactional
    public UserDto.SwitchRoleResponse switchRole(java.util.UUID userId, UserDto.SwitchRoleRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new DomainException(UserErrorCode.USER_NOT_FOUND));

        User.Role targetRole = request.getRole();
        validateSwitchTargetRole(targetRole);

        if (targetRole == User.Role.EDITOR && !editorProfileRepository.existsByUserId(userId)) {
            throw new DomainException(UserErrorCode.EDITOR_PROFILE_REQUIRED_FOR_SWITCH);
        }

        user.assignRole(targetRole);
        userRepository.save(user);

        String accessToken = jwtTokenProvider.createAccessToken(new CustomOAuth2User(user, java.util.Map.of()));
        return UserDto.SwitchRoleResponse.builder()
                .accessToken(accessToken)
                .role(targetRole)
                .build();
    }

    private void validateSwitchTargetRole(User.Role targetRole) {
        if (targetRole != User.Role.ARCHIVER && targetRole != User.Role.EDITOR) {
            throw new DomainException(UserErrorCode.INVALID_ROLE_SWITCH_TARGET);
        }
    }
}
