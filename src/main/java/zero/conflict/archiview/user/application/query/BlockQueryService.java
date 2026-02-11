package zero.conflict.archiview.user.application.query;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import zero.conflict.archiview.global.error.DomainException;
import zero.conflict.archiview.user.application.port.out.EditorBlockRepository;
import zero.conflict.archiview.user.application.port.out.EditorProfileRepository;
import zero.conflict.archiview.user.application.port.out.UserRepository;
import zero.conflict.archiview.user.domain.EditorBlock;
import zero.conflict.archiview.user.domain.EditorProfile;
import zero.conflict.archiview.user.domain.User;
import zero.conflict.archiview.user.domain.error.UserErrorCode;
import zero.conflict.archiview.user.dto.BlockDto;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BlockQueryService {

    private final EditorBlockRepository editorBlockRepository;
    private final UserRepository userRepository;
    private final EditorProfileRepository editorProfileRepository;

    public BlockDto.ListResponse getMyBlockedEditors(UUID archiverId) {
        User archiver = userRepository.findById(archiverId)
                .orElseThrow(() -> new DomainException(UserErrorCode.USER_NOT_FOUND));
        if (archiver.getRole() != User.Role.ARCHIVER) {
            throw new DomainException(UserErrorCode.INVALID_FOLLOWER_ROLE);
        }

        List<EditorBlock> blocks = editorBlockRepository.findAllByArchiverId(archiverId);
        if (blocks.isEmpty()) {
            return BlockDto.ListResponse.empty();
        }

        List<EditorBlock> orderedBlocks = blocks.stream()
                .sorted(Comparator.comparing(EditorBlock::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder()))
                        .reversed())
                .toList();

        List<UUID> editorIds = orderedBlocks.stream()
                .map(EditorBlock::getEditorId)
                .toList();

        List<EditorProfile> profiles = editorProfileRepository.findAllByUserIds(editorIds);
        if (profiles.size() != editorIds.size()) {
            throw new DomainException(UserErrorCode.EDITOR_PROFILE_NOT_FOUND);
        }

        Map<UUID, Integer> orderByEditorId = new HashMap<>();
        Map<UUID, java.time.LocalDateTime> blockedAtByEditorId = new HashMap<>();
        for (int index = 0; index < orderedBlocks.size(); index++) {
            EditorBlock block = orderedBlocks.get(index);
            orderByEditorId.put(block.getEditorId(), index);
            blockedAtByEditorId.put(block.getEditorId(), block.getCreatedAt());
        }

        List<BlockDto.BlockedEditorResponse> editors = profiles.stream()
                .sorted(Comparator.comparing(
                        profile -> orderByEditorId.getOrDefault(profile.getUser().getId(), Integer.MAX_VALUE)))
                .map(profile -> BlockDto.BlockedEditorResponse.from(
                        profile,
                        blockedAtByEditorId.get(profile.getUser().getId())))
                .toList();

        return BlockDto.ListResponse.from(editors);
    }
}
