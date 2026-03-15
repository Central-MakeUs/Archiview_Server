package zero.conflict.archiview.post.presentation.command.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import zero.conflict.archiview.post.presentation.command.api.ArchiverPostCommandApi;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/archivers/me")
public class ArchiverPostCommandController implements ArchiverPostCommandApi {


}
