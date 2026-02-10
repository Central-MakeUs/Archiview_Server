package zero.conflict.archiview.user.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import zero.conflict.archiview.global.domain.BaseTimeEntity;
import zero.conflict.archiview.user.dto.SearchDto;

import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Table(
        name = "search_history",
        uniqueConstraints = @UniqueConstraint(columnNames = { "archiver_id", "keyword_normalized" }))
public class SearchHistory extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "archiver_id", nullable = false, columnDefinition = "BINARY(16)")
    private UUID archiverId;

    @Column(name = "keyword", nullable = false)
    private String keyword;

    @Column(name = "keyword_normalized", nullable = false)
    private String keywordNormalized;

    @Enumerated(EnumType.STRING)
    @Column(name = "keyword_type", nullable = false)
    private SearchDto.KeywordType keywordType;

    public static SearchHistory createOf(
            UUID archiverId,
            String keyword,
            String keywordNormalized,
            SearchDto.KeywordType keywordType) {
        return SearchHistory.builder()
                .archiverId(archiverId)
                .keyword(keyword)
                .keywordNormalized(keywordNormalized)
                .keywordType(keywordType)
                .build();
    }

    public void updateKeyword(String keyword, SearchDto.KeywordType keywordType) {
        this.keyword = keyword;
        this.keywordType = keywordType;
    }
}
