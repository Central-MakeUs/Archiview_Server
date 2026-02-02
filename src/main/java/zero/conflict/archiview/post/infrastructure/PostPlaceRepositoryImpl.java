package zero.conflict.archiview.post.infrastructure;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import zero.conflict.archiview.post.application.port.out.PostPlaceRepository;
import zero.conflict.archiview.post.domain.PostPlace;

import java.util.List;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class PostPlaceRepositoryImpl implements PostPlaceRepository {

    private final PostPlaceJpaRepository postPlaceJpaRepository;

    @Override
    public PostPlace save(PostPlace postPlace) {
        return postPlaceJpaRepository.save(postPlace);
    }

    @Override
    public List<PostPlace> findAllByEditorId(UUID editorId) {
        return postPlaceJpaRepository.findAllByEditorId(editorId);
    }

    @Override
    public List<PostPlace> findAllByEditorIdAndPlaceId(UUID editorId, UUID placeId) {
        return postPlaceJpaRepository.findAllByEditorIdAndPlaceId(editorId, placeId);
    }
}
