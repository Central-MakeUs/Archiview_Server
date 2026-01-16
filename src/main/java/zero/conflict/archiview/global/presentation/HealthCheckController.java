package zero.conflict.archiview.global.presentation;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import zero.conflict.archiview.global.infra.response.ApiResponse;

import java.util.Map;

@RestController
public class HealthCheckController {

    @GetMapping("/health")
    public ResponseEntity<ApiResponse<Map<String, String>>> health() {
        return ResponseEntity.ok(ApiResponse.success(Map.of("status", "ok")));
    }

    @GetMapping("/")
    public ResponseEntity<ApiResponse<Map<String, String>>> root() {
        return ResponseEntity.ok(ApiResponse.success(Map.of("status", "ok")));
    }
}
