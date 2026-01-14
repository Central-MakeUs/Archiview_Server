package zero.conflict.archiview.post.infrastructure;

import org.springframework.stereotype.Repository;
import zero.conflict.archiview.post.application.port.out.PostRepository;
import zero.conflict.archiview.post.domain.Post;

@Repository
public class PostRepositoryImpl implements PostRepository {
    @Override
    public Post save(Post post) {
        return null;
    }
}
