package zero.conflict.archiview.user.application.command;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import zero.conflict.archiview.global.error.DomainException;
import zero.conflict.archiview.user.application.port.EditorProfileRepository;
import zero.conflict.archiview.user.application.port.UserRepository;
import zero.conflict.archiview.user.domain.EditorProfile;
import zero.conflict.archiview.user.domain.Hashtags;
import zero.conflict.archiview.user.domain.User;
import zero.conflict.archiview.user.domain.error.UserErrorCode;
import zero.conflict.archiview.user.dto.EditorProfileDto;

@Service
@RequiredArgsConstructor
public class EditorProfileCommandService {

    private final UserRepository userRepository;
    private final EditorProfileRepository editorProfileRepository;

    @Transactional
    public EditorProfileDto.Response createProfile(java.util.UUID userId, EditorProfileDto.CreateRequest request) {
        User user = getEditorUser(userId);

        if (editorProfileRepository.existsByUserId(userId)) {
            throw new DomainException(UserErrorCode.EDITOR_PROFILE_ALREADY_EXISTS);
        }

        validateNickname(null, request.getNickname());
        validateInstagramId(null, request.getInstagramId());

        EditorProfile profile = EditorProfile.createOf(
                user,
                request.getNickname(),
                request.getIntroduction(),
                request.getInstagramId(),
                request.getInstagramUrl(),
                null,
                Hashtags.of(request.getHashtags().get(0), request.getHashtags().get(1)));

        EditorProfile savedProfile = editorProfileRepository.save(profile);

        return EditorProfileDto.Response.from(savedProfile);
    }

    @Transactional
    public EditorProfileDto.Response updateProfile(java.util.UUID userId, EditorProfileDto.UpdateRequest request) {
        User user = getEditorUser(userId);

        EditorProfile profile = editorProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new DomainException(UserErrorCode.EDITOR_PROFILE_NOT_FOUND));

        validateNickname(profile.getNickname(), request.getNickname());
        validateInstagramId(profile.getInstagramId(), request.getInstagramId());

        profile.update(
                request.getNickname(),
                request.getIntroduction(),
                request.getInstagramId(),
                request.getInstagramUrl(),
                request.getProfileImageUrl() == null ? profile.getProfileImageUrl() : request.getProfileImageUrl(),
                Hashtags.of(request.getHashtags().get(0), request.getHashtags().get(1)));

        return EditorProfileDto.Response.from(profile);
    }

    private User getEditorUser(java.util.UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new DomainException(UserErrorCode.USER_NOT_FOUND));

        if (user.getRole() != User.Role.EDITOR) {
            throw new DomainException(UserErrorCode.INVALID_ROLE_FOR_EDITOR_PROFILE);
        }

        return user;
    }

    private void validateNickname(String currentNickname, String nickname) {
        if (nickname != null && !nickname.equals(currentNickname)
                && editorProfileRepository.existsByNickname(nickname)) {
            throw new DomainException(UserErrorCode.DUPLICATE_NICKNAME);
        }
    }

    private void validateInstagramId(String currentInstagramId, String instagramId) {
        if (instagramId != null && !instagramId.equals(currentInstagramId)
                && editorProfileRepository.existsByInstagramId(instagramId)) {
            throw new DomainException(UserErrorCode.DUPLICATE_INSTAGRAM_ID);
        }
    }
}
