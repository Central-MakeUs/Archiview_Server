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
    @Query("select distinct pp from PostPlace pp join fetch pp.place join fetch pp.post left join fetch pp.postPlaceCategories ppc left join fetch ppc.category where pp.editorId = :editorId")
    List<PostPlace> findAllByEditorId(@Param("editorId") UUID editorId);

    @Query("select distinct pp from PostPlace pp join fetch pp.place join fetch pp.post left join fetch pp.postPlaceCategories ppc left join fetch ppc.category where pp.id in :ids")
    List<PostPlace> findAllByIdIn(@Param("ids") List<Long> ids);

    void deleteAllByPost_Id(Long postId);

    void deleteAllByIdIn(List<Long> ids);

    @Query("select distinct pp from PostPlace pp join fetch pp.place join fetch pp.post left join fetch pp.postPlaceCategories ppc left join fetch ppc.category where pp.place.id in :placeIds")
    List<PostPlace> findAllByPlace_IdIn(@Param("placeIds") List<Long> placeIds);

    @Query("select distinct pp from PostPlace pp join fetch pp.place join fetch pp.post left join fetch pp.postPlaceCategories ppc left join fetch ppc.category where pp.place.id = :placeId")
    List<PostPlace> findAllByPlace_Id(@Param("placeId") Long placeId);

    @Query("select distinct pp from PostPlace pp join fetch pp.place join fetch pp.post left join fetch pp.postPlaceCategories ppc left join fetch ppc.category where pp.post.id = :postId")
    List<PostPlace> findAllByPost_Id(@Param("postId") Long postId);

    @Query("select distinct pp from PostPlace pp join fetch pp.place join fetch pp.post left join fetch pp.postPlaceCategories ppc left join fetch ppc.category where pp.editorId = :editorId and pp.place.id = :placeId")
    List<PostPlace> findAllByEditorIdAndPlaceId(@Param("editorId") UUID editorId, @Param("placeId") Long placeId);

    @Query("select distinct pp from PostPlace pp join fetch pp.place join fetch pp.post left join fetch pp.postPlaceCategories ppc left join fetch ppc.category where pp.editorId in :editorIds")
    List<PostPlace> findAllByEditorIdIn(@Param("editorIds") List<UUID> editorIds);

    @Query("select distinct pp from PostPlace pp join fetch pp.place join fetch pp.post left join fetch pp.postPlaceCategories ppc left join fetch ppc.category where ppc.category.id = :categoryId")
    List<PostPlace> findAllByPostPlaceCategories_Category_Id(@Param("categoryId") Long categoryId);

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
