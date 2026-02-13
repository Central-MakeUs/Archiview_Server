package zero.conflict.archiview.post.infrastructure.persistence;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;
import zero.conflict.archiview.post.domain.Place;

import java.util.List;

public interface CategoryPlaceReadRepository extends Repository<Place, Long> {

    @Query(value = """
            SELECT
                p.id AS placeId,
                p.name AS placeName,
                (
                    SELECT pp2.description
                    FROM post_place pp2
                    WHERE pp2.place_id = p.id
                    ORDER BY pp2.created_at DESC, pp2.id DESC
                    LIMIT 1
                ) AS latestDescription,
                COALESCE(p.view_count, 0) AS viewCount,
                COALESCE((
                    SELECT SUM(pp3.save_count)
                    FROM post_place pp3
                    WHERE pp3.place_id = p.id
                ), 0) AS saveCount
            FROM place p
            WHERE EXISTS (
                SELECT 1
                FROM post_place pp
                JOIN post_place_categories ppc ON ppc.post_place_id = pp.id
                WHERE pp.place_id = p.id
                  AND ppc.category_id = :categoryId
            )
            ORDER BY (
                SELECT pp4.created_at
                FROM post_place pp4
                WHERE pp4.place_id = p.id
                ORDER BY pp4.created_at DESC, pp4.id DESC
                LIMIT 1
            ) DESC, p.id DESC
            """, nativeQuery = true)
    List<CategoryPlaceSummaryProjection> findPlaceSummariesByCategoryId(@Param("categoryId") Long categoryId);

    @Query("""
            SELECT p
            FROM Place p
            WHERE function(
                'ST_Distance_Sphere',
                function('POINT', p.position.longitude, p.position.latitude),
                function('POINT', :longitude, :latitude)
            ) <= :radiusMeter
            """)
    List<Place> findPlacesNearby(
            @Param("latitude") Double latitude,
            @Param("longitude") Double longitude,
            @Param("radiusMeter") Integer radiusMeter);

    interface CategoryPlaceSummaryProjection {
        Long getPlaceId();

        String getPlaceName();

        String getLatestDescription();

        Long getViewCount();

        Long getSaveCount();
    }
}
