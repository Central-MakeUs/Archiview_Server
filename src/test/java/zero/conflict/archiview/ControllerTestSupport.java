package zero.conflict.archiview;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import zero.conflict.archiview.auth.domain.CustomOAuth2User;
import zero.conflict.archiview.user.domain.User;

import java.util.Map;
import java.util.UUID;

public abstract class ControllerTestSupport {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    protected CustomOAuth2User createMockUser() {
        User testUser = User.builder()
                .id(UUID.fromString("00000000-0000-0000-0000-000000000001"))
                .email("test@example.com")
                .name("testuser")
                .role(User.Role.EDITOR)
                .build();

        return new CustomOAuth2User(testUser, Map.of("id", testUser.getId()));
    }

    protected RequestPostProcessor authenticatedUser() {
        return request -> {
            CustomOAuth2User customOAuth2User = createMockUser();
            UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(
                    customOAuth2User,
                    null,
                    customOAuth2User.getAuthorities()
            );

            // Principal을 request attribute로 직접 설정
            request.setAttribute("SPRING_SECURITY_CONTEXT",
                SecurityContextHolder.createEmptyContext());
            SecurityContext context = (SecurityContext) request.getAttribute("SPRING_SECURITY_CONTEXT");
            context.setAuthentication(token);

            // 세션에도 설정
            request.getSession(true).setAttribute(
                "SPRING_SECURITY_CONTEXT", context);

            return request;
        };
    }

    @BeforeEach
    void setUp() {
        // 테스트용 User 객체 생성
        User testUser = User.builder()
                .id(UUID.fromString("00000000-0000-0000-0000-000000000001"))
                .email("test@example.com")
                .name("testuser")
                .role(User.Role.EDITOR)
                .build();

        // User 객체를 사용하여 CustomOAuth2User 생성
        CustomOAuth2User customOAuth2User = new CustomOAuth2User(
                testUser,
                Map.of("id", testUser.getId())
        );

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(
                customOAuth2User,
                "",
                customOAuth2User.getAuthorities()
        );
        context.setAuthentication(token);
        SecurityContextHolder.setContext(context);
    }
}
