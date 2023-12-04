package ua.com.obox.security.bucket4j;

import io.github.bucket4j.Bucket;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static java.time.Duration.ofMinutes;

@Service
public class RateLimiterService {

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();
    private final Map<HttpMethod, RateLimitConfig> rateLimitConfigs = new EnumMap<>(HttpMethod.class);

    @Value("${bucket4j.get.capacity}")
    private long getCapacity;
    @Value("${bucket4j.get.regenerate}")
    private long getRegenerate;
    @Value("${bucket4j.post.capacity}")
    private long postCapacity;
    @Value("${bucket4j.post.regenerate}")
    private long postRegenerate;
    @Value("${bucket4j.patch.capacity}")
    private long patchCapacity;
    @Value("${bucket4j.patch.regenerate}")
    private long patchRegenerate;
    @Value("${bucket4j.delete.capacity}")
    private long deleteCapacity;
    @Value("${bucket4j.delete.regenerate}")
    private long deleteRegenerate;

    @PostConstruct
    private void initRateLimitConfigs() {
        rateLimitConfigs.put(HttpMethod.GET, new RateLimitConfig(getCapacity, getRegenerate));
        rateLimitConfigs.put(HttpMethod.POST, new RateLimitConfig(postCapacity, postRegenerate));
        rateLimitConfigs.put(HttpMethod.PATCH, new RateLimitConfig(patchCapacity, patchRegenerate));
        rateLimitConfigs.put(HttpMethod.DELETE, new RateLimitConfig(deleteCapacity, deleteRegenerate));
    }

    public boolean isAllowed(String ipAddress, HttpMethod method) {
        RateLimitConfig config = rateLimitConfigs.get(method);

        buckets.compute(ipAddress.concat(method.name()), (key, existingBucket) -> {
            if (existingBucket == null) {
                return Bucket.builder()
                        .addLimit(limit -> limit.capacity(config.getCapacity())
                                .refillGreedy(config.getRegenerate(), ofMinutes(1)))
                        .build();
            } else {
                return existingBucket;
            }
        });

        return buckets.get(ipAddress.concat(method.name())).tryConsume(1);
    }
}
