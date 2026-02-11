package zero.conflict.archiview.post.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import zero.conflict.archiview.global.domain.BaseTimeEntity;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class PostPlace extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "place_id")
    private Place place;

    @Column(name = "editor_id", nullable = false, columnDefinition = "BINARY(16)")
    private UUID editorId;

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

    public static PostPlace createOf(Post post, Place place, String description, String imageUrl,
            UUID editorId) {
        return PostPlace.builder()
                .post(post)
                .place(place)
                .description(description)
                .imageUrl(imageUrl)
                .editorId(editorId)
                .build();
    }

    public void addCategory(Category category) {
        PostPlaceCategory postPlaceCategory = PostPlaceCategory.createOf(this, category);
        this.postPlaceCategories.add(postPlaceCategory);
    }

    public void increaseViewCount(UUID actorId) {
        if (!this.editorId.equals(actorId)) {
            this.viewCount++;
            if (this.place != null) {
                this.place.increaseViewCount();
            }
        }
    }

    public void increaseSaveCount(UUID actorId) {
        if (!this.editorId.equals(actorId)) {
            this.saveCount++;
        }
    }

    public void decreaseSaveCount(UUID actorId) {
        if (this.editorId.equals(actorId)) {
            return;
        }
        if (this.saveCount == null || this.saveCount <= 0L) {
            this.saveCount = 0L;
            return;
        }
        this.saveCount--;
    }

    public void increaseInstagramInflowCount(UUID actorId) {
        if (!this.editorId.equals(actorId)) {
            this.instagramInflowCount++;
        }
    }

    public void increaseDirectionCount(UUID actorId) {
        if (!this.editorId.equals(actorId)) {
            this.directionCount++;
        }
    }

}
