package zero.conflict.archiview.post.infrastructure.redis;

public enum PostPlaceCountMetric {
    VIEW("viewCount"),
    SAVE("saveCount"),
    INSTAGRAM_INFLOW("instagramInflowCount"),
    DIRECTION("directionCount");

    private final String field;

    PostPlaceCountMetric(String field) {
        this.field = field;
    }

    public String field() {
        return field;
    }
}
