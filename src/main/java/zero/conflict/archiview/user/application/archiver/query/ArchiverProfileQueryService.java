package zero.conflict.archiview.user.application.archiver.query;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import zero.conflict.archiview.global.error.DomainException;
import zero.conflict.archiview.user.application.port.out.ArchiverProfileRepository;
import zero.conflict.archiview.user.application.port.out.UserRepository;
import zero.conflict.archiview.user.application.support.NicknameGenerator;
import zero.conflict.archiview.user.domain.ArchiverProfile;
import zero.conflict.archiview.user.domain.User;
import zero.conflict.archiview.user.domain.error.UserErrorCode;
import zero.conflict.archiview.user.dto.ArchiverProfileDto;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class ArchiverProfileQueryService {

    private final ArchiverProfileRepository archiverProfileRepository;
    private final UserRepository userRepository;
    private final NicknameGenerator nicknameGenerator;

    public ArchiverProfileDto.Response getMyProfile(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new DomainException(UserErrorCode.USER_NOT_FOUND));
        if (user.getRole() != User.Role.ARCHIVER && user.getRole() != User.Role.EDITOR) {
            throw new DomainException(UserErrorCode.INVALID_SEARCHER_ROLE);
        }

        ArchiverProfile profile = archiverProfileRepository.findByUserId(userId)
                .orElseGet(() -> archiverProfileRepository.save(
                        ArchiverProfile.createOf(user, nicknameGenerator.generate())));
        return ArchiverProfileDto.Response.from(profile);
    }

}
