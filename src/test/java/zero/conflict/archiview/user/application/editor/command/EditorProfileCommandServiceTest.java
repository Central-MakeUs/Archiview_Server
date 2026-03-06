package zero.conflict.archiview.user.application.editor.command;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import zero.conflict.archiview.global.error.DomainException;
import zero.conflict.archiview.user.application.port.out.EditorProfileRepository;
import zero.conflict.archiview.user.application.port.out.UserRepository;
import zero.conflict.archiview.user.domain.User;
import zero.conflict.archiview.user.domain.error.UserErrorCode;
import zero.conflict.archiview.user.dto.EditorProfileDto;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class EditorProfileCommandServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private EditorProfileRepository editorProfileRepository;

    @InjectMocks
    private EditorProfileCommandService editorProfileCommandService;

    @Test
    @DisplayName("에디터 프로필 등록 시 닉네임 중복이면 USER_003 예외를 반환한다")
    void createProfile_duplicateNickname_throwsDomainException() {
        UUID userId = UUID.randomUUID();
        User user = User.builder()
                .id(userId)
                .email("editor@archiview.com")
                .name("editor")
                .provider(User.OAuthProvider.KAKAO)
                .providerId("kakao-editor")
                .role(User.Role.EDITOR)
                .build();

        EditorProfileDto.CreateRequest request = EditorProfileDto.CreateRequest.builder()
                .nickname("중복닉네임")
                .instagramId("editor_insta")
                .instagramUrl("instagram.com/editor_insta")
                .introduction("소개")
                .hashtags(java.util.List.of("#A", "#B"))
                .build();

        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(editorProfileRepository.existsByUserId(userId)).willReturn(false);
        given(editorProfileRepository.existsByNickname("중복닉네임")).willReturn(false);
        given(editorProfileRepository.existsByInstagramId("editor_insta")).willReturn(false);
        given(editorProfileRepository.save(any()))
                .willThrow(new DataIntegrityViolationException("duplicate nickname"));

        assertThatThrownBy(() -> editorProfileCommandService.createProfile(userId, request))
                .isInstanceOf(DomainException.class)
                .extracting(ex -> ((DomainException) ex).getErrorCode())
                .isEqualTo(UserErrorCode.DUPLICATE_NICKNAME);
    }
}
