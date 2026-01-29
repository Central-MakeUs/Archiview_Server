package zero.conflict.archiview.post.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import zero.conflict.archiview.global.domain.BaseTimeEntity;
import zero.conflict.archiview.user.domain.User;

import java.util.ArrayList;
import java.util.List;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "editor_id")
    private User editor;

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
            zero.conflict.archiview.user.domain.User editor) {
        return PostPlace.builder()
                .post(post)
                .place(place)
                .description(description)
                .imageUrl(imageUrl)
                .editor(editor)
                .build();
    }

    public void addCategory(Category category) {
        PostPlaceCategory postPlaceCategory = PostPlaceCategory.createOf(this, category);
        this.postPlaceCategories.add(postPlaceCategory);
    }

    public void increaseViewCount(Long actorId) {
        if (!this.editor.getId().equals(actorId)) {
            this.viewCount++;
        }
    }

    public void increaseSaveCount(Long actorId) {
        if (!this.editor.getId().equals(actorId)) {
            this.saveCount++;
        }
    }

    public void increaseInstagramInflowCount(Long actorId) {
        if (!this.editor.getId().equals(actorId)) {
            this.instagramInflowCount++;
        }
    }

    public void increaseDirectionCount(Long actorId) {
        if (!this.editor.getId().equals(actorId)) {
            this.directionCount++;
        }
    }

}
