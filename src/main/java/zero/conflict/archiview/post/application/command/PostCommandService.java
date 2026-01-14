package zero.conflict.archiview.post.application.command;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import zero.conflict.archiview.post.application.command.dto.PostCommandDto;
import zero.conflict.archiview.post.application.port.out.PlaceRepository;
import zero.conflict.archiview.post.application.port.out.PostPlaceRepository;
import zero.conflict.archiview.post.application.port.out.PostRepository;
import zero.conflict.archiview.post.domain.*;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PostCommandService {

    private final PostRepository postRepository;
    private final PlaceRepository placeRepository;
    private final PostPlaceRepository postPlacesRepository;

    @Transactional
    public PostCommandDto.Response createPost(PostCommandDto.Request request, Long editorId) {
        Post post = Post.createOf(editorId, request.getUrl(), request.getHashTag());
        Post savedPost = postRepository.save(post);

        List<PostCommandDto.Response.PlaceInfoResponse> placeInfoResponses =
            createPlacesAndPostPlaces(request.getPlaceInfoRequestList(), savedPost.getId());

        return mapPostToResponse(savedPost, placeInfoResponses);
    }

    private List<PostCommandDto.Response.PlaceInfoResponse> createPlacesAndPostPlaces(
            List<PostCommandDto.Request.PlaceInfoRequest> placeInfoRequests,
            Long postId) {

        List<PostCommandDto.Response.PlaceInfoResponse> responses = new ArrayList<>();

        for (PostCommandDto.Request.PlaceInfoRequest placeInfo : placeInfoRequests) {
            Position position = Position.of(placeInfo.getLatitude(), placeInfo.getLongitude());

            // 기존 Place 찾기 또는 새로 생성
            Place savedPlace = placeRepository.findByPosition(position)
                    .orElseGet(() -> {
                        Place newPlace = createPlace(placeInfo);
                        return placeRepository.save(newPlace);
                    });

            PostPlaces postPlace = PostPlaces.createOf(postId, savedPlace.getId(), placeInfo.getDescription());
            postPlacesRepository.save(postPlace);

            responses.add(mapPlaceToResponse(savedPlace));
        }

        return responses;
    }


    private Place createPlace(PostCommandDto.Request.PlaceInfoRequest placeInfo) {
        return Place.createOf(
            placeInfo.getName(),
            Address.of(
                placeInfo.getRoadAddress(),
                placeInfo.getDetailAddress(),
                placeInfo.getZipCode()
            ),
            Position.of(
                placeInfo.getLatitude(),
                placeInfo.getLongitude()
            )
        );
    }

    private static PostCommandDto.Response mapPostToResponse(
            Post savedPost,
            List<PostCommandDto.Response.PlaceInfoResponse> placeInfoResponses) {
        return PostCommandDto.Response.of(
            savedPost.getId(),
            savedPost.getUrl(),
            savedPost.getHashTag(),
            placeInfoResponses
        );
    }

    private static PostCommandDto.Response.PlaceInfoResponse mapPlaceToResponse(Place place) {
        return PostCommandDto.Response.PlaceInfoResponse.of(
            place.getId(),
            place.getName(),
            place.getAddress().getRoadAddress(),
            place.getAddress().getDetailAddress(),
            place.getAddress().getZipCode(),
            place.getPosition().getLatitude(),
            place.getPosition().getLongitude()
        );
    }
}