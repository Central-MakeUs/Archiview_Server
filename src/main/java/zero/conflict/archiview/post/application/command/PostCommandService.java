package zero.conflict.archiview.post.application.command;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import zero.conflict.archiview.post.application.command.dto.PostCommandDto;
import zero.conflict.archiview.post.application.port.out.CategoryRepository;
import zero.conflict.archiview.post.application.port.out.PlaceRepository;
import zero.conflict.archiview.post.application.port.out.PostPlaceRepository;
import zero.conflict.archiview.post.application.port.out.PostRepository;
import zero.conflict.archiview.global.error.DomainException;
import zero.conflict.archiview.post.domain.Address;
import zero.conflict.archiview.post.domain.Category;
import zero.conflict.archiview.post.domain.Place;
import zero.conflict.archiview.post.domain.Position;
import zero.conflict.archiview.post.domain.Post;
import zero.conflict.archiview.post.domain.PostPlace;
import zero.conflict.archiview.post.domain.error.PostErrorCode;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PostCommandService {

    private final PostRepository postRepository;
    private final PlaceRepository placeRepository;
    private final PostPlaceRepository postPlacesRepository;
    private final CategoryRepository categoryRepository;

    @Transactional
    public PostCommandDto.Response createPost(PostCommandDto.Request request, Long editorId) {
        validatePostRequest(request);

        Post post = Post.createOf(editorId, request.getUrl(), request.getHashTag());
        Post savedPost = postRepository.save(post);

        List<PostCommandDto.Response.PlaceInfoResponse> placeInfoResponses = createPlacesAndPostPlaces(
                request.getPlaceInfoRequestList(), savedPost.getId(), editorId);

        return mapPostToResponse(savedPost, placeInfoResponses);
    }

    private void validatePostRequest(PostCommandDto.Request request) {
        // 인스타그램 URL 검증
        if (request.getUrl() == null || !request.getUrl().startsWith("https://www.instagram.com/")) {
            throw new DomainException(PostErrorCode.INVALID_INSTAGRAM_URL);
        }

        // 해시태그 개수 검증 (최대 3개)
        if (request.getHashTag() != null && !request.getHashTag().isBlank()) {
            String[] tags = request.getHashTag().trim().split("\\s+");
            if (tags.length > 3) {
                throw new DomainException(PostErrorCode.TOO_MANY_HASHTAGS);
            }
        }
    }

    private List<PostCommandDto.Response.PlaceInfoResponse> createPlacesAndPostPlaces(
            List<PostCommandDto.Request.PlaceInfoRequest> placeInfoRequests,
            Long postId,
            Long editorId) {

        List<PostCommandDto.Response.PlaceInfoResponse> responses = new ArrayList<>();

        for (PostCommandDto.Request.PlaceInfoRequest placeInfo : placeInfoRequests) {
            Position position = Position.of(placeInfo.getLatitude(), placeInfo.getLongitude());

            // 기존 Place 찾기 또는 새로 생성
            Place savedPlace = placeRepository.findByPosition(position)
                    .orElseGet(() -> {
                        Place newPlace = createPlace(placeInfo);
                        return placeRepository.save(newPlace);
                    });

            PostPlace postPlace = PostPlace.createOf(
                    postId,
                    savedPlace.getId(),
                    placeInfo.getDescription(),
                    placeInfo.getImageUrl(),
                    editorId);

            if (placeInfo.getCategoryIds() != null) {
                for (Long categoryId : placeInfo.getCategoryIds()) {
                    if (categoryId == null) {
                        throw new DomainException(PostErrorCode.INVALID_CATEGORY_ID);
                    }
                    Category category = categoryRepository.findById(categoryId)
                            .orElseThrow(() -> new DomainException(PostErrorCode.INVALID_CATEGORY_ID));
                    postPlace.addCategory(category);
                }
            }
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
                        placeInfo.getZipCode()),
                Position.of(
                        placeInfo.getLatitude(),
                        placeInfo.getLongitude()));
    }

    private static PostCommandDto.Response mapPostToResponse(
            Post savedPost,
            List<PostCommandDto.Response.PlaceInfoResponse> placeInfoResponses) {
        return PostCommandDto.Response.of(
                savedPost.getId(),
                savedPost.getUrl(),
                savedPost.getHashTag(),
                placeInfoResponses);
    }

    private static PostCommandDto.Response.PlaceInfoResponse mapPlaceToResponse(Place place) {
        return PostCommandDto.Response.PlaceInfoResponse.of(
                place.getId(),
                place.getName(),
                place.getAddress().getRoadAddress(),
                place.getAddress().getDetailAddress(),
                place.getAddress().getZipCode(),
                place.getPosition().getLatitude(),
                place.getPosition().getLongitude());
    }
}
