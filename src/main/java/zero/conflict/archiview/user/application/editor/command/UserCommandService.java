package zero.conflict.archiview.user.application.editor.command;

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
import zero.conflict.archiview.user.dto.EditorProfileDto;
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
    private final EditorProfileCommandService editorProfileCommandService;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public void completeOnboarding(java.util.UUID userId, UserDto.OnboardingRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new DomainException(UserErrorCode.USER_NOT_FOUND));

        if (user.getRole() != User.Role.GUEST) {
            throw new DomainException(UserErrorCode.ALREADY_ONBOARDED);
        }

        User.Role targetRole = request.getRole();
        validateSwitchTargetRole(targetRole);
        user.assignRole(targetRole);

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

        if (targetRole == User.Role.ARCHIVER && archiverProfileRepository.findByUserId(userId).isEmpty()) {
            String nickname = nicknameGenerator.generate();
            ArchiverProfile profile = ArchiverProfile.createOf(user, nickname);
            archiverProfileRepository.save(profile);
        }

        if (targetRole == User.Role.EDITOR
                && user.getRole() != User.Role.EDITOR
                && !editorProfileRepository.existsByUserId(userId)) {
            throw new DomainException(UserErrorCode.EDITOR_PROFILE_REQUIRED_FOR_SWITCH);
        }

        // View switch API should not downgrade persisted role from EDITOR.
        if (targetRole == User.Role.EDITOR && user.getRole() != User.Role.EDITOR) {
            user.assignRole(User.Role.EDITOR);
            userRepository.save(user);
        }

        String accessToken = jwtTokenProvider.createAccessToken(
                new CustomOAuth2User(copyWithRole(user, targetRole), java.util.Map.of()));
        return UserDto.SwitchRoleResponse.builder()
                .accessToken(accessToken)
                .role(targetRole)
                .build();
    }

    @Transactional
    public UserDto.RegisterEditorProfileResponse registerEditorProfile(
            java.util.UUID userId,
            EditorProfileDto.CreateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new DomainException(UserErrorCode.USER_NOT_FOUND));

        if (user.getRole() == User.Role.GUEST) {
            throw new DomainException(UserErrorCode.ONBOARDING_REQUIRED_FOR_EDITOR_PROFILE);
        }

        if (user.getRole() == User.Role.ARCHIVER) {
            user.assignRole(User.Role.EDITOR);
            userRepository.save(user);
        }

        EditorProfileDto.Response editorProfile = editorProfileCommandService.createProfile(userId, request);
        String accessToken = jwtTokenProvider.createAccessToken(new CustomOAuth2User(user, java.util.Map.of()));

        return UserDto.RegisterEditorProfileResponse.builder()
                .accessToken(accessToken)
                .role(user.getRole())
                .editorProfile(editorProfile)
                .build();
    }

    private void validateSwitchTargetRole(User.Role targetRole) {
        if (targetRole != User.Role.ARCHIVER && targetRole != User.Role.EDITOR) {
            throw new DomainException(UserErrorCode.INVALID_ROLE_SWITCH_TARGET);
        }
    }

    private User copyWithRole(User user, User.Role role) {
        return User.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .provider(user.getProvider())
                .providerId(user.getProviderId())
                .role(role)
                .build();
    }
}
