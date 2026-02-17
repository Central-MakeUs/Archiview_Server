package zero.conflict.archiview.post.infrastructure.persistence;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import zero.conflict.archiview.post.domain.PostPlace;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface PostPlaceJpaRepository extends JpaRepository<PostPlace, Long> {
    List<PostPlace> findAllByEditorId(UUID editorId);

    List<PostPlace> findAllByIdIn(List<Long> postPlaceIds);

    void deleteAllByPost_Id(Long postId);

    void deleteAllByIdIn(List<Long> ids);

    List<PostPlace> findAllByPlace_IdIn(List<Long> placeIds);

    List<PostPlace> findAllByPlace_Id(Long placeId);

    List<PostPlace> findAllByPost_Id(Long postId);

    List<PostPlace> findAllByEditorIdAndPlaceId(UUID editorId, Long placeId);

    List<PostPlace> findAllByEditorIdIn(List<UUID> editorIds);

    List<PostPlace> findAllByPostPlaceCategories_Category_Id(Long categoryId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update PostPlace pp
            set pp.deletedAt = :deletedAt, pp.deletedBy = :actorId
            where pp.post.id = :postId and pp.deletedAt is null
            """)
    int markDeletedAllByPostId(
            @Param("postId") Long postId,
            @Param("actorId") UUID actorId,
            @Param("deletedAt") LocalDateTime deletedAt);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("""
            update PostPlace pp
            set pp.viewCount = coalesce(pp.viewCount, 0) + :viewDelta,
                pp.saveCount = coalesce(pp.saveCount, 0) + :saveDelta,
                pp.instagramInflowCount = coalesce(pp.instagramInflowCount, 0) + :instagramInflowDelta,
                pp.directionCount = coalesce(pp.directionCount, 0) + :directionDelta
            where pp.id = :postPlaceId and pp.deletedAt is null
            """)
    int applyCountDeltas(
            @Param("postPlaceId") Long postPlaceId,
            @Param("viewDelta") long viewDelta,
            @Param("saveDelta") long saveDelta,
            @Param("instagramInflowDelta") long instagramInflowDelta,
            @Param("directionDelta") long directionDelta);
}
