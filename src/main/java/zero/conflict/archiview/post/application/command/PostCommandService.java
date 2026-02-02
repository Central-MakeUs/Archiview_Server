package zero.conflict.archiview.post.application.command;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import zero.conflict.archiview.post.application.command.dto.PostCommandDto;
import zero.conflict.archiview.post.application.command.dto.PresignedUrlCommandDto;
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

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PostCommandService {

    private final PostRepository postRepository;
    private final PlaceRepository placeRepository;
    private final PostPlaceRepository postPlacesRepository;
    private final CategoryRepository categoryRepository;
    private final zero.conflict.archiview.user.application.port.UserRepository userRepository;
    private final zero.conflict.archiview.global.infra.s3.S3Service s3Service;

    @Transactional
    public PostCommandDto.Response createPost(PostCommandDto.Request request, Long editorId) {
        Post post = Post.createOf(editorId, request.getUrl(), request.getHashTag());
        Post savedPost = postRepository.save(post);

        zero.conflict.archiview.user.domain.User editor = userRepository.findById(editorId)
                .orElseThrow(() -> new DomainException(
                        zero.conflict.archiview.user.domain.error.UserErrorCode.USER_NOT_FOUND));

        List<PostCommandDto.Response.PlaceInfoResponse> placeInfoResponses = createPlacesAndPostPlaces(
                request.getPlaceInfoRequestList(), savedPost, editor);

        return mapPostToResponse(savedPost, placeInfoResponses);
    }

    public PresignedUrlCommandDto.Response createPostImagePresignedUrl(PresignedUrlCommandDto.Request request) {
        PresignedUrlInfo presignedUrl = s3Service.generatePresignedUploadUrl(
                "posts",
                request.getFilename(),
                request.getContentType());

        return PresignedUrlCommandDto.Response.of(presignedUrl.uploadUrl());
    }

    private List<PostCommandDto.Response.PlaceInfoResponse> createPlacesAndPostPlaces(
            List<PostCommandDto.Request.PlaceInfoRequest> placeInfoRequests,
            Post post,
            zero.conflict.archiview.user.domain.User editor) {

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
                    editor);

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
                        placeInfo.getLongitude()),
                placeInfo.getNearestStationWalkTime());
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
