package zero.conflict.archiview.post.infrastructure.persistence;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import zero.conflict.archiview.post.infrastructure.redis.PostPlaceCountDelta;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("PostPlaceCountBulkUpdater 테스트")
class PostPlaceCountBulkUpdaterTest {

    @InjectMocks
    private PostPlaceCountBulkUpdater bulkUpdater;

    @Mock
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Test
    @DisplayName("여러 postPlace 델타를 CASE WHEN 단일 쿼리로 업데이트한다")
    void applyDeltas_bulkUpdateSingleQuery() {
        List<PostPlaceCountDelta> deltas = List.of(
                new PostPlaceCountDelta(101L, 3L, -1L, 2L, 1L),
                new PostPlaceCountDelta(102L, 1L, 2L, 0L, 4L));
        given(jdbcTemplate.update(anyString(), any(SqlParameterSource.class))).willReturn(2);

        int updated = bulkUpdater.applyDeltas(deltas);

        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<SqlParameterSource> paramCaptor = ArgumentCaptor.forClass(SqlParameterSource.class);
        verify(jdbcTemplate).update(sqlCaptor.capture(), paramCaptor.capture());

        assertThat(updated).isEqualTo(2);
        String sql = sqlCaptor.getValue();
        assertThat(sql).contains("UPDATE post_place");
        assertThat(sql).contains("CASE id WHEN :id_0 THEN :view_0 WHEN :id_1 THEN :view_1 ELSE 0 END");
        assertThat(sql).contains("GREATEST(0, COALESCE(save_count, 0) + CASE id WHEN :id_0 THEN :save_0 WHEN :id_1 THEN :save_1 ELSE 0 END)");
        assertThat(sql).contains("WHERE id IN (:id_0, :id_1)");

        SqlParameterSource params = paramCaptor.getValue();
        assertThat(params.getValue("id_0")).isEqualTo(101L);
        assertThat(params.getValue("view_0")).isEqualTo(3L);
        assertThat(params.getValue("save_0")).isEqualTo(-1L);
        assertThat(params.getValue("instagram_1")).isEqualTo(0L);
        assertThat(params.getValue("direction_1")).isEqualTo(4L);
    }

    @Test
    @DisplayName("빈 델타 리스트면 업데이트를 실행하지 않는다")
    void applyDeltas_empty_noOp() {
        int updated = bulkUpdater.applyDeltas(List.of());

        assertThat(updated).isEqualTo(0);
        verify(jdbcTemplate, never()).update(anyString(), any(SqlParameterSource.class));
    }
}
