package zero.conflict.archiview.post.infrastructure.redis;

public record PostPlaceCountDelta(
        Long postPlaceId,
        long viewDelta,
        long saveDelta,
        long instagramInflowDelta,
        long directionDelta) {

    public boolean isEmpty() {
        return viewDelta == 0L
                && saveDelta == 0L
                && instagramInflowDelta == 0L
                && directionDelta == 0L;
    }
}
