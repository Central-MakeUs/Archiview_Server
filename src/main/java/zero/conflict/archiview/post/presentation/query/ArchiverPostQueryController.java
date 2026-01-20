package zero.conflict.archiview.post.presentation.query;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/archivers/me/posts")
@Tag(name = "Archiver Post Query", description = "아카이버 게시글/장소 조회 API")
public class ArchiverPostQueryController {
}
