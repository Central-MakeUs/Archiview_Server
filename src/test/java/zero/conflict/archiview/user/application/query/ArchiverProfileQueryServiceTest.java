package zero.conflict.archiview.user.application.query;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import zero.conflict.archiview.global.error.DomainException;
import zero.conflict.archiview.user.application.archiver.query.ArchiverProfileQueryService;
import zero.conflict.archiview.user.application.port.out.ArchiverProfileRepository;
import zero.conflict.archiview.user.application.port.out.UserRepository;
import zero.conflict.archiview.user.application.support.NicknameGenerator;
import zero.conflict.archiview.user.domain.ArchiverProfile;
import zero.conflict.archiview.user.domain.User;
import zero.conflict.archiview.user.domain.error.UserErrorCode;
import zero.conflict.archiview.user.dto.ArchiverProfileDto;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@DisplayName("ArchiverProfileQueryService 테스트")
class ArchiverProfileQueryServiceTest {

    @InjectMocks
    private ArchiverProfileQueryService archiverProfileQueryService;

    @Mock
    private ArchiverProfileRepository archiverProfileRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private NicknameGenerator nicknameGenerator;

    @Test
    @DisplayName("ARCHIVER가 프로필이 있으면 기존 프로필을 반환한다")
    void getMyProfile_archiverWithExistingProfile_success() {
        UUID userId = UUID.randomUUID();
        User user = User.builder().id(userId).role(User.Role.ARCHIVER).build();
        ArchiverProfile profile = ArchiverProfile.builder().id(1L).user(user).nickname("기존 닉네임").build();

        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(archiverProfileRepository.findByUserId(userId)).willReturn(Optional.of(profile));

        ArchiverProfileDto.Response response = archiverProfileQueryService.getMyProfile(userId);

        assertThat(response.getUserId()).isEqualTo(userId);
        assertThat(response.getNickname()).isEqualTo("기존 닉네임");
    }

    @Test
    @DisplayName("EDITOR가 프로필이 없으면 새로 생성한다")
    void getMyProfile_editorWithoutProfile_createsProfile() {
        UUID userId = UUID.randomUUID();
        User user = User.builder().id(userId).role(User.Role.EDITOR).build();
        ArchiverProfile savedProfile = ArchiverProfile.builder().id(2L).user(user).nickname("생성 닉네임").build();

        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(archiverProfileRepository.findByUserId(userId)).willReturn(Optional.empty());
        given(nicknameGenerator.generate()).willReturn("생성 닉네임");
        given(archiverProfileRepository.save(any(ArchiverProfile.class))).willReturn(savedProfile);

        ArchiverProfileDto.Response response = archiverProfileQueryService.getMyProfile(userId);

        assertThat(response.getUserId()).isEqualTo(userId);
        assertThat(response.getNickname()).isEqualTo("생성 닉네임");
    }

    @Test
    @DisplayName("GUEST는 아카이버 프로필 조회를 할 수 없다")
    void getMyProfile_guest_forbidden() {
        UUID userId = UUID.randomUUID();
        given(userRepository.findById(userId))
                .willReturn(Optional.of(User.builder().id(userId).role(User.Role.GUEST).build()));

        assertThatThrownBy(() -> archiverProfileQueryService.getMyProfile(userId))
                .isInstanceOf(DomainException.class)
                .satisfies(ex -> assertThat(((DomainException) ex).getErrorCode())
                        .isEqualTo(UserErrorCode.INVALID_SEARCHER_ROLE));
    }
}
