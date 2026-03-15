package zero.conflict.archiview.post.presentation.command;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/archivers/me")
public class ArchiverPostCommandController implements ArchiverPostCommandApi {


}
