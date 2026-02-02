package zero.conflict.archiview.global.infra.s3;

public record PresignedUrlInfo(String uploadUrl, String key, long expiresInSeconds) {
}
