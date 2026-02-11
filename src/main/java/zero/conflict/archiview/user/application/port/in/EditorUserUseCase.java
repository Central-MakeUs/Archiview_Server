package zero.conflict.archiview.user.application.port.in;

import zero.conflict.archiview.user.dto.EditorProfileDto;
import zero.conflict.archiview.user.dto.UserDto;

import java.util.UUID;

public interface EditorUserUseCase {

    EditorProfileDto.Response createProfile(UUID userId, EditorProfileDto.CreateRequest request);

    EditorProfileDto.Response updateProfile(UUID userId, EditorProfileDto.UpdateRequest request);

    EditorProfileDto.Response getMyProfile(UUID userId);

    EditorProfileDto.Response getEditorProfile(UUID editorId);

    EditorProfileDto.ArchiverViewResponse getEditorProfileForArchiver(UUID editorId);

    boolean existsInstagramId(String instagramId);

    void completeOnboarding(UUID userId, UserDto.OnboardingRequest request);

    UserDto.SwitchRoleResponse switchRole(UUID userId, UserDto.SwitchRoleRequest request);
}
