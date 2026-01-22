package zero.conflict.archiview.post.application.port.out;

import zero.conflict.archiview.post.domain.Post;

import java.util.List;

public interface PostRepository {

    Post save(Post post);

    List<Post> findAllByIds(List<Long> ids);
}
