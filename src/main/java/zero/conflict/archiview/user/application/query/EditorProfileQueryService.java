package zero.conflict.archiview.user.application.query;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import zero.conflict.archiview.global.error.DomainException;
import zero.conflict.archiview.user.application.port.EditorProfileRepository;
import zero.conflict.archiview.user.domain.EditorProfile;
import zero.conflict.archiview.user.domain.error.UserErrorCode;
import zero.conflict.archiview.user.dto.EditorProfileDto;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EditorProfileQueryService {

    private final EditorProfileRepository editorProfileRepository;

    @Transactional(readOnly = true)
    public EditorProfileDto.Response getMyProfile(java.util.UUID userId) {
        EditorProfile profile = editorProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new DomainException(UserErrorCode.EDITOR_PROFILE_NOT_FOUND));

        return mapToResponse(profile);
    }

    @Transactional(readOnly = true)
    public EditorProfileDto.Response getEditorProfile(java.util.UUID editorId) {
        EditorProfile profile = editorProfileRepository.findByUserId(editorId)
                .orElseThrow(() -> new DomainException(UserErrorCode.EDITOR_PROFILE_NOT_FOUND));

        return mapToResponse(profile);
    }

    @Transactional(readOnly = true)
    public boolean existsInstagramId(String instagramId) {
        return editorProfileRepository.existsByInstagramId(instagramId);
    }

    private EditorProfileDto.Response mapToResponse(EditorProfile profile) {
        return EditorProfileDto.Response.builder()
                .nickname(profile.getNickname())
                .instagramId(profile.getInstagramId())
                .instagramUrl(profile.getInstagramUrl())
                .introduction(profile.getIntroduction())
                .hashtags(List.of(profile.getHashtags().getFirst(), profile.getHashtags().getSecond()))
                .profileImageUrl(profile.getProfileImageUrl())
                .build();
    }
}
