package zero.conflict.archiview.user.application.editor.command;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import zero.conflict.archiview.global.error.DomainException;
import zero.conflict.archiview.user.application.port.out.EditorProfileRepository;
import zero.conflict.archiview.user.application.port.out.UserRepository;
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
        String normalizedInstagramUrl = normalizeInstagramUrl(request.getInstagramUrl());

        EditorProfile profile = EditorProfile.createOf(
                user,
                request.getNickname(),
                request.getIntroduction(),
                request.getInstagramId(),
                normalizedInstagramUrl,
                request.getProfileImageUrl(),
                Hashtags.of(request.getHashtags().get(0), request.getHashtags().get(1)));

        EditorProfile savedProfile;
        try {
            savedProfile = editorProfileRepository.save(profile);
        } catch (DataIntegrityViolationException e) {
            throw resolveDuplicateProfileException(e, userId, request.getNickname(), request.getInstagramId());
        }

        return EditorProfileDto.Response.from(savedProfile);
    }

    @Transactional
    public EditorProfileDto.Response updateProfile(java.util.UUID userId, EditorProfileDto.UpdateRequest request) {
        User user = getEditorUser(userId);

        EditorProfile profile = editorProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new DomainException(UserErrorCode.EDITOR_PROFILE_NOT_FOUND));

        validateNickname(profile.getNickname(), request.getNickname());
        validateInstagramId(profile.getInstagramId(), request.getInstagramId());
        String normalizedInstagramUrl = normalizeInstagramUrl(request.getInstagramUrl());

        profile.update(
                request.getNickname(),
                request.getIntroduction(),
                request.getInstagramId(),
                normalizedInstagramUrl,
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

    private String normalizeInstagramUrl(String instagramUrl) {
        String normalized = instagramUrl;
        if (!normalized.startsWith("https://")) {
            normalized = "https://" + normalized;
        }

        return normalized.replaceFirst("^https://www\\.", "https://");
    }

    private DomainException resolveDuplicateProfileException(DataIntegrityViolationException exception,
            java.util.UUID userId, String nickname, String instagramId) {
        if (editorProfileRepository.existsByUserId(userId)) {
            throw new DomainException(UserErrorCode.EDITOR_PROFILE_ALREADY_EXISTS);
        }
        if (nickname != null && editorProfileRepository.existsByNickname(nickname)) {
            throw new DomainException(UserErrorCode.DUPLICATE_NICKNAME);
        }
        if (instagramId != null && editorProfileRepository.existsByInstagramId(instagramId)) {
            throw new DomainException(UserErrorCode.DUPLICATE_INSTAGRAM_ID);
        }

        String message = buildMostSpecificExceptionMessage(exception);
        if (containsAny(message, "nickname", "nick_name")) {
            throw new DomainException(UserErrorCode.DUPLICATE_NICKNAME);
        }
        if (containsAny(message, "instagram_id", "instagramid")) {
            throw new DomainException(UserErrorCode.DUPLICATE_INSTAGRAM_ID);
        }
        throw new DomainException(UserErrorCode.EDITOR_PROFILE_ALREADY_EXISTS);
    }

    private String buildMostSpecificExceptionMessage(Throwable throwable) {
        Throwable current = throwable;
        while (current != null && current.getCause() != null) {
            current = current.getCause();
        }
        if (current == null || current.getMessage() == null) {
            return "";
        }
        return current.getMessage().toLowerCase();
    }

    private boolean containsAny(String value, String... candidates) {
        for (String candidate : candidates) {
            if (value.contains(candidate)) {
                return true;
            }
        }
        return false;
    }
}
