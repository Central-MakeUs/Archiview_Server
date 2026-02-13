package zero.conflict.archiview.user.application.archiver.command;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import zero.conflict.archiview.global.error.DomainException;
import zero.conflict.archiview.user.application.port.out.FollowRepository;
import zero.conflict.archiview.user.application.port.out.UserRepository;
import zero.conflict.archiview.user.domain.Follow;
import zero.conflict.archiview.user.domain.User;
import zero.conflict.archiview.user.domain.error.UserErrorCode;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FollowCommandService {

    private final FollowRepository followRepository;
    private final UserRepository userRepository;

    public void follow(UUID archiverId, UUID editorId) {
        User archiver = userRepository.findById(archiverId)
                .orElseThrow(() -> new DomainException(UserErrorCode.USER_NOT_FOUND));
        User editor = userRepository.findById(editorId)
                .orElseThrow(() -> new DomainException(UserErrorCode.USER_NOT_FOUND));


        if (editor.getRole() != User.Role.EDITOR) {
            throw new DomainException(UserErrorCode.INVALID_FOLLOWEE_ROLE);
        }
        if (followRepository.existsByArchiverIdAndEditorId(archiverId, editorId)) {
            throw new DomainException(UserErrorCode.FOLLOW_ALREADY_EXISTS);
        }

        followRepository.save(Follow.createOf(archiverId, editorId));
    }

    public void unfollow(UUID archiverId, UUID editorId) {
        if (!followRepository.existsByArchiverIdAndEditorId(archiverId, editorId)) {
            throw new DomainException(UserErrorCode.FOLLOW_NOT_FOUND);
        }
        followRepository.deleteByArchiverIdAndEditorId(archiverId, editorId);
    }
}
