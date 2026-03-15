package zero.conflict.archiview.global.presentation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import zero.conflict.archiview.global.infra.response.ApiResponse;

import java.util.Map;

@Tag(name = "Health", description = "서버 상태 확인 API")
public interface HealthCheckApi {

    @Operation(summary = "헬스체크", description = "서버 상태를 확인합니다. 성공 시 status=ok 를 반환합니다.")
    ResponseEntity<ApiResponse<Map<String, String>>> health();

    @Operation(summary = "루트 상태 조회", description = "루트 경로에서 서버 상태를 간단히 확인합니다. 성공 시 status=ok 를 반환합니다.")
    ResponseEntity<ApiResponse<Map<String, String>>> root();
}
