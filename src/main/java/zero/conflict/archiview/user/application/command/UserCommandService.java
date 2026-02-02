package zero.conflict.archiview.user.application.command;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import zero.conflict.archiview.global.error.DomainException;
import zero.conflict.archiview.user.application.port.UserRepository;
import zero.conflict.archiview.user.domain.User;
import zero.conflict.archiview.user.domain.error.UserErrorCode;
import zero.conflict.archiview.user.presentation.dto.UserDto;

@Service
@RequiredArgsConstructor
public class UserCommandService {

    private final UserRepository userRepository;

    @Transactional
    public void completeOnboarding(java.util.UUID userId, UserDto.OnboardingRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new DomainException(UserErrorCode.USER_NOT_FOUND));

        if (user.getRole() != User.Role.GUEST) {
            throw new DomainException(UserErrorCode.ALREADY_ONBOARDED);
        }

        user.assignRole(request.getRole());
        userRepository.save(user);
    }
}
