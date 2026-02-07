package zero.conflict.archiview.post.application.command;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import zero.conflict.archiview.post.dto.PostCommandDto;
import zero.conflict.archiview.post.dto.PresignedUrlCommandDto;
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
import zero.conflict.archiview.global.infra.s3.PresignedUrlInfo;
import zero.conflict.archiview.global.infra.s3.S3Service;
import zero.conflict.archiview.user.application.port.UserRepository;
import zero.conflict.archiview.user.domain.User;
import zero.conflict.archiview.user.domain.error.UserErrorCode;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PostCommandService {

    private final PostRepository postRepository;
    private final PlaceRepository placeRepository;
    private final PostPlaceRepository postPlacesRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final S3Service s3Service;

    @Transactional
    public PostCommandDto.Response createPost(PostCommandDto.Request request, java.util.UUID editorId) {
        Post post = Post.createOf(editorId, request.getUrl(), request.getHashTags());
        Post savedPost = postRepository.save(post);

        User editor = userRepository.findById(editorId)
                .orElseThrow(() -> new DomainException(UserErrorCode.USER_NOT_FOUND));

        List<PostCommandDto.Response.PlaceInfoResponse> placeInfoResponses = createPlacesAndPostPlaces(
                request.getPlaceInfoRequestList(), savedPost, editor.getId());

        return mapPostToResponse(savedPost, placeInfoResponses);
    }

    public PresignedUrlCommandDto.Response createPostImagePresignedUrl(PresignedUrlCommandDto.Request request) {
        PresignedUrlInfo presignedUrl = s3Service.generatePresignedUploadUrl(
                "posts",
                request.getFilename(),
                request.getContentType());

        return PresignedUrlCommandDto.Response.of(presignedUrl.uploadUrl());
    }

    @Transactional
    public void increasePostPlaceViewCount(Long postPlaceId, java.util.UUID actorId) {
        PostPlace postPlace = postPlacesRepository.findById(postPlaceId)
                .orElseThrow(() -> new DomainException(PostErrorCode.POST_PLACE_NOT_FOUND));

        postPlace.increaseViewCount(actorId);
    }

    @Transactional
    public PostCommandDto.Response updatePost(
            Long postId,
            PostCommandDto.Request request,
            java.util.UUID editorId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new DomainException(PostErrorCode.POST_NOT_FOUND));

        if (!post.getEditorId().equals(editorId)) {
            throw new DomainException(PostErrorCode.POST_FORBIDDEN);
        }

        post.update(request.getUrl(), request.getHashTags());
        postRepository.save(post);

        postPlacesRepository.deleteAllByPostId(postId);

        List<PostCommandDto.Response.PlaceInfoResponse> placeInfoResponses = createPlacesAndPostPlaces(
                request.getPlaceInfoRequestList(), post, editorId);

        return mapPostToResponse(post, placeInfoResponses);
    }

    private List<PostCommandDto.Response.PlaceInfoResponse> createPlacesAndPostPlaces(
            List<PostCommandDto.Request.PlaceInfoRequest> placeInfoRequests,
            Post post,
            java.util.UUID editorId) {

        List<PostCommandDto.Response.PlaceInfoResponse> responses = new ArrayList<>();

        for (int i = 0; i < placeInfoRequests.size(); i++) {
            PostCommandDto.Request.PlaceInfoRequest placeInfo = placeInfoRequests.get(i);
            Position position = Position.of(placeInfo.getLatitude(), placeInfo.getLongitude());

            String imageUrl = placeInfo.getImageUrl();

            // 기존 Place 찾기 또는 새로 생성
            Place savedPlace = placeRepository.findByPosition(position)
                    .orElseGet(() -> {
                        Place newPlace = createPlace(placeInfo);
                        return placeRepository.save(newPlace);
                    });

            PostPlace postPlace = PostPlace.createOf(
                    post,
                    savedPlace,
                    placeInfo.getDescription(),
                    imageUrl,
                    editorId);

            List<Long> categoryIds = placeInfo.getCategoryIds();
            if (categoryIds != null && !categoryIds.isEmpty()) {
                for (Long categoryId : categoryIds) {
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
                placeInfo.getResolvedPlaceName(),
                Address.of(
                        placeInfo.getAddressName(),
                        placeInfo.getRoadAddressName()),
                Position.of(
                        placeInfo.getLatitude(),
                        placeInfo.getLongitude()),
                placeInfo.getNearestStationWalkTime(),
                placeInfo.getPlaceUrl(),
                placeInfo.getPhoneNumber());
    }

    private static PostCommandDto.Response mapPostToResponse(
            Post savedPost,
            List<PostCommandDto.Response.PlaceInfoResponse> placeInfoResponses) {
        return PostCommandDto.Response.from(savedPost, placeInfoResponses);
    }

    private static PostCommandDto.Response.PlaceInfoResponse mapPlaceToResponse(Place place) {
        return PostCommandDto.Response.PlaceInfoResponse.from(place);
    }
}
