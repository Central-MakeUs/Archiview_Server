package zero.conflict.archiview.user.application.command;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import zero.conflict.archiview.auth.infrastructure.JwtTokenProvider;
import zero.conflict.archiview.global.error.DomainException;
import zero.conflict.archiview.user.application.port.out.ArchiverProfileRepository;
import zero.conflict.archiview.user.application.port.out.EditorProfileRepository;
import zero.conflict.archiview.user.application.port.out.UserRepository;
import zero.conflict.archiview.user.application.support.NicknameGenerator;
import zero.conflict.archiview.user.domain.User;
import zero.conflict.archiview.user.domain.error.UserErrorCode;
import zero.conflict.archiview.user.dto.UserDto;

import java.lang.reflect.Field;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UserCommandServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private NicknameGenerator nicknameGenerator;
    @Mock
    private ArchiverProfileRepository archiverProfileRepository;
    @Mock
    private EditorProfileRepository editorProfileRepository;
    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private UserCommandService userCommandService;

    @Test
    @DisplayName("ARCHIVER로 전환 시 아카이버 토큰을 발급한다")
    void switchRole_toArchiver_success() throws Exception {
        UUID userId = UUID.fromString("00000000-0000-0000-0000-000000000101");
        User user = createUser(userId, User.Role.EDITOR);
        UserDto.SwitchRoleRequest request = new UserDto.SwitchRoleRequest();
        setRole(request, User.Role.ARCHIVER);

        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(jwtTokenProvider.createAccessToken(any())).willReturn("archiver-access-token");

        UserDto.SwitchRoleResponse response = userCommandService.switchRole(userId, request);

        assertThat(response.getAccessToken()).isEqualTo("archiver-access-token");
        assertThat(response.getRole()).isEqualTo(User.Role.ARCHIVER);
        assertThat(user.getRole()).isEqualTo(User.Role.ARCHIVER);
        verify(userRepository).save(user);
    }

    @Test
    @DisplayName("EDITOR로 전환 시 에디터 프로필이 없으면 예외를 반환한다")
    void switchRole_toEditor_withoutProfile_throwsException() throws Exception {
        UUID userId = UUID.fromString("00000000-0000-0000-0000-000000000102");
        User user = createUser(userId, User.Role.ARCHIVER);
        UserDto.SwitchRoleRequest request = new UserDto.SwitchRoleRequest();
        setRole(request, User.Role.EDITOR);

        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(editorProfileRepository.existsByUserId(userId)).willReturn(false);

        assertThatThrownBy(() -> userCommandService.switchRole(userId, request))
                .isInstanceOf(DomainException.class)
                .extracting(ex -> ((DomainException) ex).getErrorCode())
                .isEqualTo(UserErrorCode.EDITOR_PROFILE_REQUIRED_FOR_SWITCH);
    }

    @Test
    @DisplayName("EDITOR로 전환 시 에디터 프로필이 있으면 에디터 토큰을 발급한다")
    void switchRole_toEditor_withProfile_success() throws Exception {
        UUID userId = UUID.fromString("00000000-0000-0000-0000-000000000103");
        User user = createUser(userId, User.Role.ARCHIVER);
        UserDto.SwitchRoleRequest request = new UserDto.SwitchRoleRequest();
        setRole(request, User.Role.EDITOR);

        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(editorProfileRepository.existsByUserId(userId)).willReturn(true);
        given(jwtTokenProvider.createAccessToken(any())).willReturn("editor-access-token");

        UserDto.SwitchRoleResponse response = userCommandService.switchRole(userId, request);

        assertThat(response.getAccessToken()).isEqualTo("editor-access-token");
        assertThat(response.getRole()).isEqualTo(User.Role.EDITOR);
        assertThat(user.getRole()).isEqualTo(User.Role.EDITOR);
        verify(userRepository).save(user);
    }

    private User createUser(UUID userId, User.Role role) {
        return User.builder()
                .id(userId)
                .email("switch-test@archiview.com")
                .name("switch-test")
                .provider(User.OAuthProvider.KAKAO)
                .providerId("switch-test-provider-id")
                .role(role)
                .build();
    }

    private void setRole(UserDto.SwitchRoleRequest request, User.Role role) throws Exception {
        Field roleField = UserDto.SwitchRoleRequest.class.getDeclaredField("role");
        roleField.setAccessible(true);
        roleField.set(request, role);
    }
}
