package ua.com.obox.security.bucket4j;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Service
public class RateLimitConfig {
    private long capacity;
    private long regenerate;
}
