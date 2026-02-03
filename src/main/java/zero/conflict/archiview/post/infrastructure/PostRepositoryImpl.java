package zero.conflict.archiview.post.infrastructure;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import zero.conflict.archiview.post.application.port.out.PostRepository;
import zero.conflict.archiview.post.domain.Post;

import java.util.List;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class PostRepositoryImpl implements PostRepository {

    private final PostJpaRepository postJpaRepository;

    @Override
    public Post save(Post post) {
        return postJpaRepository.save(post);
    }

    @Override
    public java.util.Optional<Post> findById(UUID id) {
        return postJpaRepository.findById(id);
    }

    @Override
    public List<Post> findAllByIds(List<UUID> ids) {
        return postJpaRepository.findAllById(ids);
    }
}
