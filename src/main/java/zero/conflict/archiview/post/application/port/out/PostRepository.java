package zero.conflict.archiview.post.application.port.out;

import zero.conflict.archiview.post.domain.Post;

public interface PostRepository {

    Post save(Post post);
}
