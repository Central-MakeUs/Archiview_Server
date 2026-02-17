package zero.conflict.archiview.post.infrastructure.persistence;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;
import zero.conflict.archiview.post.infrastructure.redis.PostPlaceCountDelta;

import java.util.List;

@Component
public class PostPlaceCountBulkUpdater {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public PostPlaceCountBulkUpdater(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public int applyDeltas(List<PostPlaceCountDelta> deltas) {
        if (deltas == null || deltas.isEmpty()) {
            return 0;
        }

        MapSqlParameterSource params = new MapSqlParameterSource();
        StringBuilder idsInClause = new StringBuilder();
        StringBuilder viewCase = new StringBuilder();
        StringBuilder saveCase = new StringBuilder();
        StringBuilder instagramCase = new StringBuilder();
        StringBuilder directionCase = new StringBuilder();

        for (int i = 0; i < deltas.size(); i++) {
            PostPlaceCountDelta delta = deltas.get(i);
            if (i > 0) {
                idsInClause.append(", ");
            }

            String idKey = "id_" + i;
            String viewKey = "view_" + i;
            String saveKey = "save_" + i;
            String instagramKey = "instagram_" + i;
            String directionKey = "direction_" + i;

            idsInClause.append(':').append(idKey);

            viewCase.append(" WHEN :").append(idKey).append(" THEN :").append(viewKey);
            saveCase.append(" WHEN :").append(idKey).append(" THEN :").append(saveKey);
            instagramCase.append(" WHEN :").append(idKey).append(" THEN :").append(instagramKey);
            directionCase.append(" WHEN :").append(idKey).append(" THEN :").append(directionKey);

            params.addValue(idKey, delta.postPlaceId());
            params.addValue(viewKey, delta.viewDelta());
            params.addValue(saveKey, delta.saveDelta());
            params.addValue(instagramKey, delta.instagramInflowDelta());
            params.addValue(directionKey, delta.directionDelta());
        }

        String sql = """
                UPDATE post_place
                SET
                  view_count = COALESCE(view_count, 0) + CASE id%s ELSE 0 END,
                  save_count = GREATEST(0, COALESCE(save_count, 0) + CASE id%s ELSE 0 END),
                  instagram_inflow_count = COALESCE(instagram_inflow_count, 0) + CASE id%s ELSE 0 END,
                  direction_count = COALESCE(direction_count, 0) + CASE id%s ELSE 0 END
                WHERE id IN (%s)
                  AND deleted_at IS NULL
                """.formatted(viewCase, saveCase, instagramCase, directionCase, idsInClause);

        return jdbcTemplate.update(sql, params);
    }
}
