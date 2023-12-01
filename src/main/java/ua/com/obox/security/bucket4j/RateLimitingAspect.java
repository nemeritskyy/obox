package ua.com.obox.security.bucket4j;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import ua.com.obox.dbschema.tools.logging.IPTools;
import ua.com.obox.dbschema.tools.logging.LogLevel;
import ua.com.obox.dbschema.tools.logging.LoggingService;
import ua.com.obox.dbschema.tools.response.BadFieldsResponse;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Aspect
@Component
public class RateLimitingAspect {

    private final RateLimiterService rateLimiterService;
    private final LoggingService loggingService;
    private final List<String> blackList = new ArrayList<>();
    AtomicInteger atomicInteger = new AtomicInteger(0); // for hard tests

    @Autowired
    public RateLimitingAspect(RateLimiterService rateLimiterService, LoggingService loggingService) {
        this.rateLimiterService = rateLimiterService;
        this.loggingService = loggingService;
    }

    @Before("execution(* ua.com.obox.dbschema..*Controller.*(..)) && @annotation(org.springframework.web.bind.annotation.GetMapping)")
    public synchronized void beforeControllerMethod() {
        System.out.println("join get " + atomicInteger.incrementAndGet());
        HttpServletRequest servletRequest = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        String ipAddress = IPTools.getOriginallyIpFromHeader(servletRequest);

        if (ipAddress != null && !rateLimiterService.isAllowedGet(ipAddress)) {
            if (!blackList.contains(ipAddress)) {
                loggingService.log(LogLevel.CRITICAL, ipAddress, "TOO MANY REQUESTS");
                blackList.add(ipAddress);
            }
            throw new BadFieldsResponse(HttpStatus.TOO_MANY_REQUESTS);
        }
    }

    @Before("execution(* ua.com.obox.dbschema..*Controller.*(..)) && @annotation(org.springframework.web.bind.annotation.PostMapping)")
    public void beforePostMethod() { // post limits
        System.out.println("join post");
    }

    @Before("execution(* ua.com.obox.dbschema..*Controller.*(..)) && @annotation(org.springframework.web.bind.annotation.PatchMapping)")
    public void beforePatchMethod() { // post limits
        System.out.println("join patch");
    }

    @Before("execution(* ua.com.obox.dbschema..*Controller.*(..)) && @annotation(org.springframework.web.bind.annotation.DeleteMapping)")
    public void beforeDeleteMethod() { // post limits
        System.out.println("join delete");
    }
}