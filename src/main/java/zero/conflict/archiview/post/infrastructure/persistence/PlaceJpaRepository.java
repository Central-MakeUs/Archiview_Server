package zero.conflict.archiview.post.infrastructure.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import zero.conflict.archiview.post.domain.Place;

import java.util.Optional;

public interface PlaceJpaRepository extends JpaRepository<Place, Long> {
    Page<Place> findAllByOrderByViewCountDesc(Pageable pageable);

    Optional<Place> findByPosition_LatitudeAndPosition_Longitude(Double latitude, Double longitude);

    @Modifying(flushAutomatically = true)
    @Query("""
            update Place p
            set p.viewCount = coalesce(p.viewCount, 0) + 1
            where p.id = :placeId
            """)
    int incrementViewCount(@Param("placeId") Long placeId);
}
