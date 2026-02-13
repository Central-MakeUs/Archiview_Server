package zero.conflict.archiview.user.application.command;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import zero.conflict.archiview.auth.infrastructure.JwtTokenProvider;
import zero.conflict.archiview.global.error.DomainException;
import zero.conflict.archiview.user.application.editor.command.EditorProfileCommandService;
import zero.conflict.archiview.user.application.editor.command.UserCommandService;
import zero.conflict.archiview.user.application.port.out.ArchiverProfileRepository;
import zero.conflict.archiview.user.application.port.out.EditorProfileRepository;
import zero.conflict.archiview.user.application.port.out.UserRepository;
import zero.conflict.archiview.user.application.support.NicknameGenerator;
import zero.conflict.archiview.user.domain.User;
import zero.conflict.archiview.user.domain.error.UserErrorCode;
import zero.conflict.archiview.user.dto.EditorProfileDto;
import zero.conflict.archiview.user.dto.UserDto;

import java.lang.reflect.Field;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
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
    private EditorProfileCommandService editorProfileCommandService;
    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private UserCommandService userCommandService;

    @Test
    @DisplayName("ARCHIVER가 에디터 프로필 등록 시 EDITOR로 전환하고 토큰을 발급한다")
    void registerEditorProfile_archiver_success() {
        UUID userId = UUID.fromString("00000000-0000-0000-0000-000000000111");
        User user = createUser(userId, User.Role.ARCHIVER);
        EditorProfileDto.CreateRequest request = EditorProfileDto.CreateRequest.builder()
                .nickname("맛집탐방가")
                .instagramId("editor_insta")
                .instagramUrl("https://www.instagram.com/editor_insta")
                .introduction("소개")
                .hashtags(java.util.List.of("#A", "#B"))
                .build();

        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(editorProfileCommandService.createProfile(userId, request)).willReturn(EditorProfileDto.Response.mock());
        given(jwtTokenProvider.createAccessToken(any())).willReturn("editor-access-token");

        UserDto.RegisterEditorProfileResponse response = userCommandService.registerEditorProfile(userId, request);

        assertThat(response.getAccessToken()).isEqualTo("editor-access-token");
        assertThat(response.getRole()).isEqualTo(User.Role.EDITOR);
        assertThat(user.getRole()).isEqualTo(User.Role.EDITOR);
        assertThat(response.getEditorProfile()).isNotNull();
        verify(userRepository).save(user);
    }

    @Test
    @DisplayName("EDITOR가 에디터 프로필 등록 시 역할 유지 + 토큰 발급한다")
    void registerEditorProfile_editor_success() {
        UUID userId = UUID.fromString("00000000-0000-0000-0000-000000000112");
        User user = createUser(userId, User.Role.EDITOR);
        EditorProfileDto.CreateRequest request = EditorProfileDto.CreateRequest.builder()
                .nickname("맛집탐방가")
                .instagramId("editor_insta")
                .instagramUrl("https://www.instagram.com/editor_insta")
                .introduction("소개")
                .hashtags(java.util.List.of("#A", "#B"))
                .build();

        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(editorProfileCommandService.createProfile(userId, request)).willReturn(EditorProfileDto.Response.mock());
        given(jwtTokenProvider.createAccessToken(any())).willReturn("editor-access-token");

        UserDto.RegisterEditorProfileResponse response = userCommandService.registerEditorProfile(userId, request);

        assertThat(response.getAccessToken()).isEqualTo("editor-access-token");
        assertThat(response.getRole()).isEqualTo(User.Role.EDITOR);
        assertThat(user.getRole()).isEqualTo(User.Role.EDITOR);
        verify(userRepository, never()).save(user);
    }

    @Test
    @DisplayName("GUEST는 에디터 프로필 등록할 수 없다")
    void registerEditorProfile_guest_throwsException() {
        UUID userId = UUID.fromString("00000000-0000-0000-0000-000000000113");
        User user = createUser(userId, User.Role.GUEST);
        EditorProfileDto.CreateRequest request = EditorProfileDto.CreateRequest.builder()
                .nickname("맛집탐방가")
                .instagramId("editor_insta")
                .instagramUrl("https://www.instagram.com/editor_insta")
                .introduction("소개")
                .hashtags(java.util.List.of("#A", "#B"))
                .build();

        given(userRepository.findById(userId)).willReturn(Optional.of(user));

        assertThatThrownBy(() -> userCommandService.registerEditorProfile(userId, request))
                .isInstanceOf(DomainException.class)
                .extracting(ex -> ((DomainException) ex).getErrorCode())
                .isEqualTo(UserErrorCode.ONBOARDING_REQUIRED_FOR_EDITOR_PROFILE);
    }

    @Test
    @DisplayName("온보딩 완료 시 GUEST 역할 선택은 예외를 반환한다")
    void completeOnboarding_guestRole_throwsException() throws Exception {
        UUID userId = UUID.fromString("00000000-0000-0000-0000-000000000114");
        User user = createUser(userId, User.Role.GUEST);
        UserDto.OnboardingRequest request = new UserDto.OnboardingRequest();
        setOnboardingRole(request, User.Role.GUEST);

        given(userRepository.findById(userId)).willReturn(Optional.of(user));

        assertThatThrownBy(() -> userCommandService.completeOnboarding(userId, request))
                .isInstanceOf(DomainException.class)
                .extracting(ex -> ((DomainException) ex).getErrorCode())
                .isEqualTo(UserErrorCode.INVALID_ROLE_SWITCH_TARGET);
    }

    @Test
    @DisplayName("EDITOR가 ARCHIVER 뷰로 전환 시 DB role은 유지하고 토큰을 발급한다")
    void switchRole_toArchiver_success() throws Exception {
        UUID userId = UUID.fromString("00000000-0000-0000-0000-000000000101");
        User user = createUser(userId, User.Role.EDITOR);
        UserDto.SwitchRoleRequest request = new UserDto.SwitchRoleRequest();
        setRole(request, User.Role.ARCHIVER);

        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(archiverProfileRepository.findByUserId(userId)).willReturn(Optional.empty());
        given(nicknameGenerator.generate()).willReturn("generated-nickname");
        given(jwtTokenProvider.createAccessToken(any())).willReturn("archiver-access-token");

        UserDto.SwitchRoleResponse response = userCommandService.switchRole(userId, request);

        assertThat(response.getAccessToken()).isEqualTo("archiver-access-token");
        assertThat(response.getRole()).isEqualTo(User.Role.ARCHIVER);
        assertThat(user.getRole()).isEqualTo(User.Role.EDITOR);
        verify(nicknameGenerator).generate();
        verify(archiverProfileRepository).save(any());
        verify(userRepository, never()).save(user);
    }

    @Test
    @DisplayName("EDITOR가 ARCHIVER 뷰로 전환 시 아카이버 프로필이 이미 있으면 생성하지 않는다")
    void switchRole_toArchiver_withExistingProfile_success() throws Exception {
        UUID userId = UUID.fromString("00000000-0000-0000-0000-000000000109");
        User user = createUser(userId, User.Role.EDITOR);
        UserDto.SwitchRoleRequest request = new UserDto.SwitchRoleRequest();
        setRole(request, User.Role.ARCHIVER);

        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(archiverProfileRepository.findByUserId(userId))
                .willReturn(Optional.of(zero.conflict.archiview.user.domain.ArchiverProfile.createOf(user, "existing")));
        given(jwtTokenProvider.createAccessToken(any())).willReturn("archiver-access-token");

        UserDto.SwitchRoleResponse response = userCommandService.switchRole(userId, request);

        assertThat(response.getAccessToken()).isEqualTo("archiver-access-token");
        assertThat(response.getRole()).isEqualTo(User.Role.ARCHIVER);
        assertThat(user.getRole()).isEqualTo(User.Role.EDITOR);
        verify(nicknameGenerator, never()).generate();
        verify(archiverProfileRepository, never()).save(any());
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

    @Test
    @DisplayName("EDITOR가 EDITOR 뷰로 전환 시 프로필 검증 없이 토큰을 발급한다")
    void switchRole_toEditor_whenAlreadyEditor_success() throws Exception {
        UUID userId = UUID.fromString("00000000-0000-0000-0000-000000000104");
        User user = createUser(userId, User.Role.EDITOR);
        UserDto.SwitchRoleRequest request = new UserDto.SwitchRoleRequest();
        setRole(request, User.Role.EDITOR);

        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(jwtTokenProvider.createAccessToken(any())).willReturn("editor-access-token");

        UserDto.SwitchRoleResponse response = userCommandService.switchRole(userId, request);

        assertThat(response.getAccessToken()).isEqualTo("editor-access-token");
        assertThat(response.getRole()).isEqualTo(User.Role.EDITOR);
        assertThat(user.getRole()).isEqualTo(User.Role.EDITOR);
        verify(editorProfileRepository, never()).existsByUserId(userId);
        verify(userRepository, never()).save(user);
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

    private void setOnboardingRole(UserDto.OnboardingRequest request, User.Role role) throws Exception {
        Field roleField = UserDto.OnboardingRequest.class.getDeclaredField("role");
        roleField.setAccessible(true);
        roleField.set(request, role);
    }
}
