package ua.com.obox.security.bucket4j;

import io.github.bucket4j.Bucket;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static java.time.Duration.ofMinutes;

@Service
public class RateLimiterService {

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

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


    public boolean isAllowedGet(String ipAddress, String type) {
        System.out.println(ipAddress.concat(type));
        buckets.computeIfAbsent(ipAddress.concat(type), k ->
                Bucket.builder().addLimit(limit -> limit.capacity(getCapacity).refillGreedy(getRegenerate, ofMinutes(1))).build());
        return buckets.get(ipAddress.concat(type)).tryConsume(1);
    }

    public boolean isAllowedPost(String ipAddress, String type) {
        buckets.computeIfAbsent(ipAddress.concat(type), k ->
                Bucket.builder().addLimit(limit -> limit.capacity(postCapacity).refillGreedy(postRegenerate, ofMinutes(1))).build());
        return buckets.get(ipAddress.concat(type)).tryConsume(1);
    }

    public boolean isAllowedPatch(String ipAddress, String type) {
        System.out.println(ipAddress.concat(type));
        buckets.computeIfAbsent(ipAddress.concat(type), k ->
                Bucket.builder().addLimit(limit -> limit.capacity(patchCapacity).refillGreedy(patchRegenerate, ofMinutes(1))).build());
        return buckets.get(ipAddress.concat(type)).tryConsume(1);
    }

    public boolean isAllowedDelete(String ipAddress, String type) {
        buckets.computeIfAbsent(ipAddress.concat(type), k ->
                Bucket.builder().addLimit(limit -> limit.capacity(deleteCapacity).refillGreedy(deleteRegenerate, ofMinutes(1))).build());
        return buckets.get(ipAddress.concat(type)).tryConsume(1);
    }
}
