package zero.conflict.archiview.post.infrastructure;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import zero.conflict.archiview.post.application.port.out.PostPlaceRepository;
import zero.conflict.archiview.post.domain.PostPlace;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class PostPlaceRepositoryImpl implements PostPlaceRepository {

    private final PostPlaceJpaRepository postPlaceJpaRepository;

    @Override
    public PostPlace save(PostPlace postPlace) {
        return postPlaceJpaRepository.save(postPlace);
    }

    @Override
    public List<PostPlace> findAllByEditorId(Long editorId) {
        return postPlaceJpaRepository.findAllByEditorId(editorId);
    }

    @Override
    public List<PostPlace> findAllByEditorIdAndPlaceId(Long editorId, Long placeId) {
        return postPlaceJpaRepository.findAllByEditorIdAndPlaceId(editorId, placeId);
    }
}
