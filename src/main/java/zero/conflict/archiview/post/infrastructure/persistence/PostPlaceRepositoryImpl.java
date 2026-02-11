package zero.conflict.archiview.post.infrastructure.persistence;

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
    public java.util.Optional<PostPlace> findById(Long postPlaceId) {
        return postPlaceJpaRepository.findById(postPlaceId);
    }

    @Override
    public List<PostPlace> findAllByIds(List<Long> postPlaceIds) {
        if (postPlaceIds == null || postPlaceIds.isEmpty()) {
            return List.of();
        }
        return postPlaceJpaRepository.findAllByIdIn(postPlaceIds);
    }

    @Override
    public void deleteAllByPostId(Long postId) {
        postPlaceJpaRepository.deleteAllByPost_Id(postId);
    }

    @Override
    public List<PostPlace> findAllByPlaceIds(List<Long> placeIds) {
        if (placeIds == null || placeIds.isEmpty()) {
            return List.of();
        }
        return postPlaceJpaRepository.findAllByPlace_IdIn(placeIds);
    }

    @Override
    public List<PostPlace> findAllByPlaceId(Long placeId) {
        return postPlaceJpaRepository.findAllByPlace_Id(placeId);
    }

    @Override
    public List<PostPlace> findAllByPostId(Long postId) {
        return postPlaceJpaRepository.findAllByPost_Id(postId);
    }

    @Override
    public List<PostPlace> findAllByEditorId(UUID editorId) {
        return postPlaceJpaRepository.findAllByEditorId(editorId);
    }

    @Override
    public List<PostPlace> findAllByEditorIdAndPlaceId(UUID editorId, Long placeId) {
        return postPlaceJpaRepository.findAllByEditorIdAndPlaceId(editorId, placeId);
    }

    @Override
    public List<PostPlace> findAllByEditorIds(List<UUID> editorIds) {
        if (editorIds == null || editorIds.isEmpty()) {
            return List.of();
        }
        return postPlaceJpaRepository.findAllByEditorIdIn(editorIds);
    }

    @Override
    public List<PostPlace> findAllByCategoryId(Long categoryId) {
        return postPlaceJpaRepository.findAllByPostPlaceCategories_Category_Id(categoryId);
    }

    @Override
    public List<PostPlace> findAll() {
        return postPlaceJpaRepository.findAll();
    }
}
