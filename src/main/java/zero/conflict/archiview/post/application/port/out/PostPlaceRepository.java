package zero.conflict.archiview.post.application.port.out;

import zero.conflict.archiview.post.domain.PostPlaces;

public interface PostPlaceRepository {

    PostPlaces save(PostPlaces postPlaces);
}
