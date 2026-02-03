package zero.conflict.archiview.user.application.command;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import zero.conflict.archiview.global.error.DomainException;
import zero.conflict.archiview.user.application.port.UserRepository;
import zero.conflict.archiview.user.domain.User;
import zero.conflict.archiview.user.domain.error.UserErrorCode;
import zero.conflict.archiview.user.presentation.dto.UserDto;
import zero.conflict.archiview.user.application.support.NicknameGenerator;
import zero.conflict.archiview.user.application.port.ArchiverProfileRepository;
import zero.conflict.archiview.user.domain.ArchiverProfile;

@Service
@RequiredArgsConstructor
public class UserCommandService {

    private final UserRepository userRepository;
    private final NicknameGenerator nicknameGenerator;
    private final ArchiverProfileRepository archiverProfileRepository;

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
}
