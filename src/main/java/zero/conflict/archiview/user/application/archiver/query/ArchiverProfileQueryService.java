package zero.conflict.archiview.user.application.archiver.query;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import zero.conflict.archiview.global.error.DomainException;
import zero.conflict.archiview.user.application.port.out.ArchiverProfileRepository;
import zero.conflict.archiview.user.domain.ArchiverProfile;
import zero.conflict.archiview.user.domain.error.UserErrorCode;
import zero.conflict.archiview.user.dto.ArchiverProfileDto;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ArchiverProfileQueryService {

    private final ArchiverProfileRepository archiverProfileRepository;

    public ArchiverProfileDto.Response getMyProfile(UUID userId) {
        ArchiverProfile profile = archiverProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new DomainException(UserErrorCode.ARCHIVER_PROFILE_NOT_FOUND));

        return ArchiverProfileDto.Response.from(profile);
    }

}
