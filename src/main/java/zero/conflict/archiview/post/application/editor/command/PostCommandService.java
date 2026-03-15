package zero.conflict.archiview.post.application.editor.command;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import zero.conflict.archiview.post.application.editor.command.event.PostOutboxService;
import zero.conflict.archiview.post.application.command.PostPlaceCountService;
import zero.conflict.archiview.post.application.port.out.CategoryRepository;
import zero.conflict.archiview.post.application.port.out.InstagramMediaStorage;
import zero.conflict.archiview.post.application.port.out.InstagramPostExtractor;
import zero.conflict.archiview.post.application.port.out.PlaceRepository;
import zero.conflict.archiview.post.application.port.out.PostPlaceRepository;
import zero.conflict.archiview.post.application.port.out.PostRepository;
import zero.conflict.archiview.post.application.port.out.UserClient;
import zero.conflict.archiview.global.error.DomainException;
import zero.conflict.archiview.post.domain.Address;
import zero.conflict.archiview.post.domain.Category;
import zero.conflict.archiview.post.domain.InstagramUrl;
import zero.conflict.archiview.post.domain.Place;
import zero.conflict.archiview.post.domain.Position;
import zero.conflict.archiview.post.domain.Post;
import zero.conflict.archiview.post.domain.PostPlace;
import zero.conflict.archiview.post.domain.error.PostErrorCode;
import zero.conflict.archiview.post.dto.InstagramPreviewDto;
import zero.conflict.archiview.post.dto.PostCommandDto;
import zero.conflict.archiview.post.dto.PresignedUrlCommandDto;
import zero.conflict.archiview.global.infra.s3.PresignedUrlInfo;
import zero.conflict.archiview.global.infra.s3.S3Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class PostCommandService {

    private final PostRepository postRepository;
    private final PlaceRepository placeRepository;
    private final PostPlaceRepository postPlacesRepository;
    private final CategoryRepository categoryRepository;
    private final UserClient userClient;
    private final InstagramPostExtractor instagramPostExtractor;
    private final InstagramMediaStorage instagramMediaStorage;
    private final S3Service s3Service;
    private final PostOutboxService postOutboxService;
    private final PostPlaceCountService postPlaceCountService;

    @Transactional
    public PostCommandDto.Response createPost(PostCommandDto.CreateRequest request, java.util.UUID editorId) {
        validateCreateRequestNoPostPlaceId(request);
        Post post = Post.createOf(editorId, request.getUrl(), request.getHashTags());
        Post savedPost = postRepository.save(post);

        if (!userClient.existsUser(editorId)) {
            throw new DomainException(PostErrorCode.POST_EDITOR_NOT_FOUND);
        }
        if (!userClient.existsEditorProfile(editorId)) {
            throw new DomainException(PostErrorCode.POST_EDITOR_PROFILE_REQUIRED);
        }

        List<PostCommandDto.Response.PlaceInfoResponse> placeInfoResponses = createPlacesAndPostPlaces(
                request.getPlaceInfoRequestList(), savedPost, editorId);
        List<Long> placeIds = placeInfoResponses.stream()
                .map(PostCommandDto.Response.PlaceInfoResponse::getPlaceId)
                .toList();
        postOutboxService.appendPostCreatedEvent(savedPost, placeIds);

        return mapPostToResponse(savedPost, placeInfoResponses);
    }

    public InstagramPreviewDto.Response previewInstagramPost(InstagramPreviewDto.Request request, java.util.UUID editorId) {
        if (!userClient.existsUser(editorId)) {
            throw new DomainException(PostErrorCode.POST_EDITOR_NOT_FOUND);
        }
        if (!userClient.existsEditorProfile(editorId)) {
            throw new DomainException(PostErrorCode.POST_EDITOR_PROFILE_REQUIRED);
        }

        String normalizedUrl = InstagramUrl.from(request.getUrl()).getValue();
        InstagramPostExtractor.ExtractedInstagramPost extracted;
        try {
            extracted = instagramPostExtractor.extract(normalizedUrl);
        } catch (DomainException e) {
            if (e.getErrorCode() != PostErrorCode.POST_INSTAGRAM_PREVIEW_UNAVAILABLE) {
                throw e;
            }
            return InstagramPreviewDto.Response.builder()
                    .sourceUrl(normalizedUrl)
                    .caption(null)
                    .hashTags(List.of())
                    .primaryImageUrl(null)
                    .allImageUrls(List.of())
                    .mediaList(List.of())
                    .extractStatus(InstagramPreviewDto.ExtractStatus.FAILED)
                    .missingFields(List.of("caption", "image"))
                    .warnings(List.of("인스타그램 게시글 미리보기를 불러오지 못했습니다. 내용을 직접 입력해 주세요."))
                    .build();
        }
        List<InstagramPostExtractor.ExtractedMedia> extractedMediaList = extracted.mediaList() == null
                ? List.of()
                : extracted.mediaList();
        List<String> extractedHashTags = extracted.hashTags() == null
                ? List.of()
                : extracted.hashTags();

        List<InstagramPreviewDto.MediaItem> mediaItems = new ArrayList<>();
        List<String> imageUrls = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        for (InstagramPostExtractor.ExtractedMedia media : extractedMediaList) {
            try {
                InstagramMediaStorage.StoredMedia storedMedia = instagramMediaStorage.store(media);
                mediaItems.add(InstagramPreviewDto.MediaItem.builder()
                        .sourceUrl(storedMedia.sourceUrl())
                        .storedUrl(storedMedia.storedUrl())
                        .mediaType(storedMedia.mediaType())
                        .build());
                imageUrls.add(storedMedia.storedUrl());
            } catch (DomainException e) {
                if (e.getErrorCode() != PostErrorCode.POST_INSTAGRAM_MEDIA_DOWNLOAD_FAILED) {
                    throw e;
                }
                warnings.add("일부 이미지를 가져오지 못했습니다.");
            }
        }

        List<String> missingFields = new ArrayList<>();
        if (extracted.caption() == null || extracted.caption().isBlank()) {
            missingFields.add("caption");
        }
        if (imageUrls.isEmpty()) {
            missingFields.add("image");
        }

        InstagramPreviewDto.ExtractStatus status = determinePreviewStatus(extracted.caption(), imageUrls);
        if (status == InstagramPreviewDto.ExtractStatus.PARTIAL_SUCCESS && warnings.isEmpty()) {
            warnings.add("일부 필드를 자동완성하지 못했습니다.");
        }
        if (status == InstagramPreviewDto.ExtractStatus.FAILED) {
            warnings.add("자동완성에 필요한 caption/image를 추출하지 못했습니다.");
        }

        return InstagramPreviewDto.Response.builder()
                .sourceUrl(extracted.sourceUrl())
                .caption(extracted.caption())
                .hashTags(extractedHashTags)
                .primaryImageUrl(imageUrls.isEmpty() ? null : imageUrls.get(0))
                .allImageUrls(imageUrls)
                .mediaList(mediaItems)
                .extractStatus(status)
                .missingFields(missingFields)
                .warnings(warnings)
                .build();
    }

    public PresignedUrlCommandDto.Response createPostImagePresignedUrl(PresignedUrlCommandDto.Request request) {
        PresignedUrlInfo presignedUrl = s3Service.generatePresignedUploadUrl(
                "posts",
                request.getFilename(),
                request.getContentType());

        String imageUrl = s3Service.getFileUrl(presignedUrl.key());

        return PresignedUrlCommandDto.Response.of(
                presignedUrl.uploadUrl(),
                imageUrl,
                presignedUrl.key());
    }

    @Transactional
    public void increasePostPlaceViewCount(Long postPlaceId, java.util.UUID actorId) {
        PostPlace postPlace = postPlacesRepository.findById(postPlaceId)
                .orElseThrow(() -> new DomainException(PostErrorCode.POST_PLACE_NOT_FOUND));

        postPlaceCountService.increaseViewCount(postPlace, actorId);
    }

    @Transactional
    public Long increasePostPlaceInstagramInflowCount(Long postPlaceId, java.util.UUID actorId) {
        PostPlace postPlace = postPlacesRepository.findById(postPlaceId)
                .orElseThrow(() -> new DomainException(PostErrorCode.POST_PLACE_NOT_FOUND));

        return postPlaceCountService.increaseInstagramInflowCount(postPlace, actorId);
    }

    @Transactional
    public Long increasePostPlaceDirectionCount(Long postPlaceId, java.util.UUID actorId) {
        PostPlace postPlace = postPlacesRepository.findById(postPlaceId)
                .orElseThrow(() -> new DomainException(PostErrorCode.POST_PLACE_NOT_FOUND));

        return postPlaceCountService.increaseDirectionCount(postPlace, actorId);
    }

    @Transactional
    public PostCommandDto.Response updatePost(
            Long postId,
            PostCommandDto.UpdateRequest request,
            java.util.UUID editorId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new DomainException(PostErrorCode.POST_NOT_FOUND));

        if (!post.getEditorId().equals(editorId)) {
            throw new DomainException(PostErrorCode.POST_FORBIDDEN);
        }

        post.update(request.getUrl(), request.getHashTags());
        postRepository.save(post);

        List<PostPlace> existingPostPlaces = postPlacesRepository.findAllByPostId(postId);
        Map<Long, PostPlace> existingById = new HashMap<>();
        for (PostPlace existingPostPlace : existingPostPlaces) {
            if (existingPostPlace.getId() != null) {
                existingById.put(existingPostPlace.getId(), existingPostPlace);
            }
        }

        Set<Long> requestedPostPlaceIds = new HashSet<>();
        for (PostCommandDto.UpdateRequest.UpdatePlaceInfoRequest placeInfo : request.getPlaceInfoRequestList()) {
            Long requestedId = placeInfo.getPostPlaceId();
            if (requestedId == null) {
                continue;
            }
            if (!requestedPostPlaceIds.add(requestedId) || !existingById.containsKey(requestedId)) {
                throw new DomainException(PostErrorCode.POST_INVALID_UPDATE_POST_PLACE_ID);
            }
        }

        List<Long> deletedPostPlaceIds = existingById.keySet().stream()
                .filter(existingId -> !requestedPostPlaceIds.contains(existingId))
                .toList();
        postPlacesRepository.deleteAllByIdIn(deletedPostPlaceIds);

        List<PostCommandDto.Response.PlaceInfoResponse> placeInfoResponses = new ArrayList<>();
        for (PostCommandDto.UpdateRequest.UpdatePlaceInfoRequest placeInfo : request.getPlaceInfoRequestList()) {
            Place savedPlace = getOrCreatePlace(placeInfo);
            List<Category> categories = resolveCategories(placeInfo.getCategoryIds());

            Long requestedPostPlaceId = placeInfo.getPostPlaceId();
            if (requestedPostPlaceId == null) {
                PostPlace postPlace = PostPlace.createOf(
                        post,
                        savedPlace,
                        placeInfo.getDescription(),
                        placeInfo.getImageUrl(),
                        editorId);
                postPlace.replaceCategories(categories);
                postPlacesRepository.save(postPlace);
            } else {
                PostPlace existingPostPlace = existingById.get(requestedPostPlaceId);
                existingPostPlace.update(savedPlace, placeInfo.getDescription(), placeInfo.getImageUrl());
                existingPostPlace.replaceCategories(categories);
                postPlacesRepository.save(existingPostPlace);
            }
            placeInfoResponses.add(mapPlaceToResponse(savedPlace));
        }

        List<Long> placeIds = placeInfoResponses.stream()
                .map(PostCommandDto.Response.PlaceInfoResponse::getPlaceId)
                .toList();
        postOutboxService.appendPostUpdatedEvent(post, placeIds);

        return mapPostToResponse(post, placeInfoResponses);
    }

    @Transactional
    public void deletePost(Long postId, java.util.UUID editorId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new DomainException(PostErrorCode.POST_NOT_FOUND));

        if (!post.getEditorId().equals(editorId)) {
            throw new DomainException(PostErrorCode.POST_FORBIDDEN);
        }

        if (post.isDeleted()) {
            return;
        }

        List<Long> placeIds = postPlacesRepository.findAllByPostId(postId).stream()
                .map(PostPlace::getPlace)
                .filter(place -> place != null && place.getId() != null)
                .map(Place::getId)
                .distinct()
                .toList();

        LocalDateTime deletedAt = LocalDateTime.now();
        post.markDeleted(deletedAt);
        postRepository.save(post);
        postPlacesRepository.markDeletedAllByPostId(postId, editorId, deletedAt);
        postOutboxService.appendPostDeletedEvent(post, placeIds);
    }

    private List<PostCommandDto.Response.PlaceInfoResponse> createPlacesAndPostPlaces(
            List<? extends PostCommandDto.PlaceInfoInput> placeInfoRequests,
            Post post,
            java.util.UUID editorId) {

        List<PostCommandDto.Response.PlaceInfoResponse> responses = new ArrayList<>();

        for (int i = 0; i < placeInfoRequests.size(); i++) {
            PostCommandDto.PlaceInfoInput placeInfo = placeInfoRequests.get(i);
            Place savedPlace = getOrCreatePlace(placeInfo);

            PostPlace postPlace = PostPlace.createOf(
                    post,
                    savedPlace,
                    placeInfo.getDescription(),
                    placeInfo.getImageUrl(),
                    editorId);
            postPlace.replaceCategories(resolveCategories(placeInfo.getCategoryIds()));
            postPlacesRepository.save(postPlace);

            responses.add(mapPlaceToResponse(savedPlace));
        }

        return responses;
    }

    private Place createPlace(PostCommandDto.PlaceInfoInput placeInfo) {
        return Place.createOf(
                placeInfo.getPlaceName(),
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

    private Place getOrCreatePlace(PostCommandDto.PlaceInfoInput placeInfo) {
        Position position = Position.of(placeInfo.getLatitude(), placeInfo.getLongitude());
        Address address = Address.of(placeInfo.getAddressName(), placeInfo.getRoadAddressName());
        Place savedPlace = placeRepository.findByIdentity(placeInfo.getPlaceName(), address, position)
                .orElseGet(() -> {
                    Place newPlace = createPlace(placeInfo);
                    return placeRepository.save(newPlace);
                });
        if (savedPlace.updatePhoneNumberIfMissing(placeInfo.getPhoneNumber())) {
            return placeRepository.save(savedPlace);
        }
        return savedPlace;
    }

    private void validateCreateRequestNoPostPlaceId(PostCommandDto.CreateRequest request) {
        for (PostCommandDto.CreateRequest.CreatePlaceInfoRequest placeInfo : request.getPlaceInfoRequestList()) {
            if (placeInfo.getPostPlaceId() != null) {
                throw new DomainException(PostErrorCode.POST_INVALID_CREATE_POST_PLACE_ID);
            }
        }
    }

    private List<Category> resolveCategories(List<Long> categoryIds) {
        if (categoryIds == null || categoryIds.isEmpty()) {
            return List.of();
        }
        Set<Long> distinctCategoryIds = new LinkedHashSet<>(categoryIds);
        List<Category> categories = new ArrayList<>();
        for (Long categoryId : distinctCategoryIds) {
            Category category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new DomainException(PostErrorCode.INVALID_CATEGORY_ID));
            categories.add(category);
        }
        return categories;
    }

    private static PostCommandDto.Response mapPostToResponse(
            Post savedPost,
            List<PostCommandDto.Response.PlaceInfoResponse> placeInfoResponses) {
        return PostCommandDto.Response.from(savedPost, placeInfoResponses);
    }

    private static PostCommandDto.Response.PlaceInfoResponse mapPlaceToResponse(Place place) {
        return PostCommandDto.Response.PlaceInfoResponse.from(place);
    }

    private InstagramPreviewDto.ExtractStatus determinePreviewStatus(String caption, List<String> imageUrls) {
        boolean hasCaption = caption != null && !caption.isBlank();
        boolean hasImage = imageUrls != null && !imageUrls.isEmpty();
        if (hasCaption && hasImage) {
            return InstagramPreviewDto.ExtractStatus.SUCCESS;
        }
        if (hasCaption || hasImage) {
            return InstagramPreviewDto.ExtractStatus.PARTIAL_SUCCESS;
        }
        return InstagramPreviewDto.ExtractStatus.FAILED;
    }
}
