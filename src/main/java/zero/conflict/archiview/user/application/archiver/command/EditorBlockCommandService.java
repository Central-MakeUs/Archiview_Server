package zero.conflict.archiview.user.application.archiver.command;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import zero.conflict.archiview.global.error.DomainException;
import zero.conflict.archiview.user.application.port.out.EditorBlockRepository;
import zero.conflict.archiview.user.application.port.out.UserRepository;
import zero.conflict.archiview.user.domain.EditorBlock;
import zero.conflict.archiview.user.domain.User;
import zero.conflict.archiview.user.domain.error.UserErrorCode;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EditorBlockCommandService {

    private final EditorBlockRepository editorBlockRepository;
    private final UserRepository userRepository;

    @Transactional
    public void blockEditor(UUID archiverId, UUID editorId) {
        User archiver = userRepository.findById(archiverId)
                .orElseThrow(() -> new DomainException(UserErrorCode.USER_NOT_FOUND));
        User editor = userRepository.findById(editorId)
                .orElseThrow(() -> new DomainException(UserErrorCode.USER_NOT_FOUND));


        if (editor.getRole() != User.Role.EDITOR) {
            throw new DomainException(UserErrorCode.INVALID_BLOCKEE_ROLE);
        }
        if (editorBlockRepository.existsByArchiverIdAndEditorId(archiverId, editorId)) {
            return;
        }

        editorBlockRepository.save(EditorBlock.createOf(archiverId, editorId));
    }

    @Transactional
    public void unblockEditor(UUID archiverId, UUID editorId) {
        editorBlockRepository.deleteByArchiverIdAndEditorId(archiverId, editorId);
    }
}
