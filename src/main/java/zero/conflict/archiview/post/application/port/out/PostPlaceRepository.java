package zero.conflict.archiview.post.application.port.out;

import zero.conflict.archiview.post.domain.PostPlace;

public interface PostPlaceRepository {

    PostPlace save(PostPlace postPlace);
}
