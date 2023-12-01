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

    public boolean isAllowedGet(String ipAddress) {
        buckets.computeIfAbsent(ipAddress, k ->
                Bucket.builder().addLimit(limit -> limit.capacity(getCapacity).refillGreedy(getRegenerate, ofMinutes(1))).build());
        return buckets.get(ipAddress).tryConsume(1);
    }
}
