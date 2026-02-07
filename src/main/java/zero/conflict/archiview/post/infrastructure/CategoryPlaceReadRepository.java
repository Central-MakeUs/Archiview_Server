package zero.conflict.archiview.post.infrastructure;

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
            ORDER BY p.view_count DESC, p.id DESC
            """, nativeQuery = true)
    List<CategoryPlaceSummaryProjection> findPlaceSummariesByCategoryId(@Param("categoryId") Long categoryId);

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
            WHERE ST_Distance_Sphere(
                POINT(p.position_longitude, p.position_latitude),
                POINT(:longitude, :latitude)
            ) <= :radiusMeter
            ORDER BY p.view_count DESC, p.id DESC
            """, nativeQuery = true)
    List<CategoryPlaceSummaryProjection> findPlaceSummariesNearby(
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
