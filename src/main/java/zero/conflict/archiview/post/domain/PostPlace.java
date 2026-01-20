package zero.conflict.archiview.post.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import zero.conflict.archiview.global.domain.BaseTimeEntity;

import java.util.ArrayList;
import java.util.List;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class PostPlace extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long postId;

    private Long placeId;

    private Long editorId;

    private String description;

    private String imageUrl;

    @OneToMany(mappedBy = "postPlace", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<PostPlaceCategory> postPlaceCategories = new ArrayList<>();

    @Builder.Default
    private Long viewCount = 0L;

    @Builder.Default
    private Long saveCount = 0L;

    @Builder.Default
    private Long instagramInflowCount = 0L;

    @Builder.Default
    private Long directionCount = 0L;

    public static PostPlace createOf(Long postId, Long placeId, String description, String imageUrl,
            Long editorId) {
        return PostPlace.builder()
                .postId(postId)
                .placeId(placeId)
                .description(description)
                .imageUrl(imageUrl)
                .editorId(editorId)
                .build();
    }

    public void addCategory(Category category) {
        PostPlaceCategory postPlaceCategory = PostPlaceCategory.createOf(this, category);
        this.postPlaceCategories.add(postPlaceCategory);
    }

    public void increaseViewCount() {
        this.viewCount++;
    }

    public void increaseSaveCount() {
        this.saveCount++;
    }

    public void increaseInstagramInflowCount() {
        this.instagramInflowCount++;
    }

    public void increaseDirectionCount() {
        this.directionCount++;
    }

}
