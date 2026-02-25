package zero.conflict.archiview.user.domain.error;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import zero.conflict.archiview.global.error.DomainErrorCode;

@Getter
@RequiredArgsConstructor
public enum UserErrorCode implements DomainErrorCode {

    USER_NOT_FOUND("USER_001", "사용자를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    ALREADY_ONBOARDED("USER_002", "이미 온보딩을 완료한 사용자입니다.", HttpStatus.BAD_REQUEST),
    DUPLICATE_NICKNAME("USER_003", "이미 사용 중인 닉네임입니다.", HttpStatus.CONFLICT),
    EDITOR_PROFILE_ALREADY_EXISTS("USER_004", "이미 에디터 프로필이 존재합니다.", HttpStatus.BAD_REQUEST),
    EDITOR_PROFILE_NOT_FOUND("USER_005", "에디터 프로필을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    ARCHIVER_PROFILE_NOT_FOUND("USER_005A", "아카이버 프로필을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    INVALID_HASHTAG("USER_006", "해시태그는 비어있을 수 없습니다.", HttpStatus.BAD_REQUEST),
    INVALID_ROLE_FOR_EDITOR_PROFILE("USER_007", "에디터만 프로필을 등록할 수 있습니다.", HttpStatus.BAD_REQUEST),
    DUPLICATE_INSTAGRAM_ID("USER_008", "이미 사용 중인 인스타그램 아이디입니다.", HttpStatus.CONFLICT),
    FOLLOW_ALREADY_EXISTS("USER_009", "이미 팔로우하고 있습니다.", HttpStatus.CONFLICT),
    FOLLOW_NOT_FOUND("USER_010", "팔로우 관계를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    INVALID_FOLLOWEE_ROLE("USER_012", "에디터만 팔로잉 대상이 될 수 있습니다.", HttpStatus.BAD_REQUEST),
    INVALID_BLOCKEE_ROLE("USER_012B", "에디터만 차단 대상이 될 수 있습니다.", HttpStatus.BAD_REQUEST),
    EDITOR_PROFILE_REQUIRED_FOR_SWITCH("USER_013", "에디터 전환을 위해 에디터 프로필 등록이 필요합니다.", HttpStatus.BAD_REQUEST),
    INVALID_ROLE_SWITCH_TARGET("USER_014", "전환 가능한 역할은 ARCHIVER 또는 EDITOR 입니다.", HttpStatus.BAD_REQUEST),
    INVALID_SEARCHER_ROLE("USER_015", "아카이버 또는 에디터만 검색할 수 있습니다.", HttpStatus.BAD_REQUEST),
    ONBOARDING_REQUIRED_FOR_EDITOR_PROFILE("USER_016", "에디터 프로필 등록 전 온보딩이 필요합니다.", HttpStatus.BAD_REQUEST),
    SELF_FOLLOW_NOT_ALLOWED("USER_017", "자기 자신은 팔로우할 수 없습니다.", HttpStatus.BAD_REQUEST),
    SELF_BLOCK_NOT_ALLOWED("USER_018", "자기 자신은 차단할 수 없습니다.", HttpStatus.BAD_REQUEST);

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;
}
