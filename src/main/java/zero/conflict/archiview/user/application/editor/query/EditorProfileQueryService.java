package zero.conflict.archiview.user.application.editor.query;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import zero.conflict.archiview.global.error.DomainException;
import zero.conflict.archiview.user.application.port.out.EditorProfileRepository;
import zero.conflict.archiview.user.application.port.out.UserRepository;
import zero.conflict.archiview.user.domain.EditorProfile;
import zero.conflict.archiview.user.domain.error.UserErrorCode;
import zero.conflict.archiview.user.dto.EditorProfileDto;


@Service
@RequiredArgsConstructor
public class EditorProfileQueryService {

    private final EditorProfileRepository editorProfileRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public EditorProfileDto.Response getMyProfile(java.util.UUID userId) {
        EditorProfile profile = editorProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new DomainException(UserErrorCode.EDITOR_PROFILE_NOT_FOUND));

        return EditorProfileDto.Response.from(profile);
    }

    @Transactional(readOnly = true)
    public EditorProfileDto.Response getEditorProfile(java.util.UUID editorId) {
        var profileOptional = editorProfileRepository.findByUserId(editorId);
        if (profileOptional.isPresent()) {
            return EditorProfileDto.Response.from(profileOptional.get());
        }

        boolean withdrawn = userRepository.findByIdIncludingDeleted(editorId)
                .map(user -> user.isDeleted())
                .orElse(false);
        if (withdrawn) {
            return EditorProfileDto.Response.withdrawn();
        }

        throw new DomainException(UserErrorCode.EDITOR_PROFILE_NOT_FOUND);
    }

    @Transactional(readOnly = true)
    public EditorProfileDto.ArchiverViewResponse getEditorProfileForArchiver(java.util.UUID editorId) {
        EditorProfile profile = editorProfileRepository.findByUserId(editorId)
                .orElseThrow(() -> new DomainException(UserErrorCode.EDITOR_PROFILE_NOT_FOUND));

        return EditorProfileDto.ArchiverViewResponse.from(editorId, profile);
    }

    @Transactional(readOnly = true)
    public boolean existsInstagramId(String instagramId) {
        return editorProfileRepository.existsByInstagramId(instagramId);
    }

    @Transactional(readOnly = true)
    public boolean existsNickname(String nickname) {
        return editorProfileRepository.existsByNickname(nickname);
    }

}
