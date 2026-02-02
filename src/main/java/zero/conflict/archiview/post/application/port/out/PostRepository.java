package zero.conflict.archiview.post.application.port.out;

import zero.conflict.archiview.post.domain.Post;

import java.util.List;
import java.util.UUID;

public interface PostRepository {

    Post save(Post post);

    List<Post> findAllByIds(List<UUID> ids);
}
