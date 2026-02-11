package zero.conflict.archiview.post.presentation.command;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Archiver Place Command", description = "아카이버용 장소 상호작용 API")
@RestController
@RequestMapping("/api/v1/archivers")
public class ArchiverPlaceCommandController {

}
