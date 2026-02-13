package zero.conflict.archiview.user.application.editor;

import zero.conflict.archiview.user.dto.EditorProfileDto;
import zero.conflict.archiview.user.dto.UserDto;

import java.util.UUID;

public interface EditorUserUseCase {

    UserDto.RegisterEditorProfileResponse registerEditorProfile(UUID userId, EditorProfileDto.CreateRequest request);

    EditorProfileDto.Response updateProfile(UUID userId, EditorProfileDto.UpdateRequest request);

    EditorProfileDto.Response getMyProfile(UUID userId);

    EditorProfileDto.Response getEditorProfile(UUID editorId);

    boolean existsInstagramId(String instagramId);

    boolean existsNickname(String nickname);

    void completeOnboarding(UUID userId, UserDto.OnboardingRequest request);

    UserDto.SwitchRoleResponse switchRole(UUID userId, UserDto.SwitchRoleRequest request);
}
