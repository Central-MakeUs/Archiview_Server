package zero.conflict.archiview.user.application.editor;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import zero.conflict.archiview.user.application.editor.command.EditorProfileCommandService;
import zero.conflict.archiview.user.application.editor.command.UserCommandService;
import zero.conflict.archiview.user.application.editor.query.EditorProfileQueryService;
import zero.conflict.archiview.user.dto.EditorProfileDto;
import zero.conflict.archiview.user.dto.UserDto;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EditorUserService implements EditorUserUseCase {

    private final EditorProfileCommandService editorProfileCommandService;
    private final EditorProfileQueryService editorProfileQueryService;
    private final UserCommandService userCommandService;

    @Override
    public EditorProfileDto.Response createProfile(UUID userId, EditorProfileDto.CreateRequest request) {
        return editorProfileCommandService.createProfile(userId, request);
    }

    @Override
    public EditorProfileDto.Response updateProfile(UUID userId, EditorProfileDto.UpdateRequest request) {
        return editorProfileCommandService.updateProfile(userId, request);
    }

    @Override
    public EditorProfileDto.Response getMyProfile(UUID userId) {
        return editorProfileQueryService.getMyProfile(userId);
    }

    @Override
    public EditorProfileDto.Response getEditorProfile(UUID editorId) {
        return editorProfileQueryService.getEditorProfile(editorId);
    }

    @Override
    public EditorProfileDto.ArchiverViewResponse getEditorProfileForArchiver(UUID editorId) {
        return editorProfileQueryService.getEditorProfileForArchiver(editorId);
    }

    @Override
    public boolean existsInstagramId(String instagramId) {
        return editorProfileQueryService.existsInstagramId(instagramId);
    }

    @Override
    public boolean existsNickname(String nickname) {
        return editorProfileQueryService.existsNickname(nickname);
    }

    @Override
    public void completeOnboarding(UUID userId, UserDto.OnboardingRequest request) {
        userCommandService.completeOnboarding(userId, request);
    }

    @Override
    public UserDto.SwitchRoleResponse switchRole(UUID userId, UserDto.SwitchRoleRequest request) {
        return userCommandService.switchRole(userId, request);
    }
}
