package ua.com.obox.security.bucket4j;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import ua.com.obox.dbschema.tools.logging.IPTools;
import ua.com.obox.dbschema.tools.logging.LogLevel;
import ua.com.obox.dbschema.tools.logging.LoggingService;
import ua.com.obox.dbschema.tools.response.BadFieldsResponse;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@Aspect
@Component
@Profile("!test")
public class RateLimitingAspect {

    private final RateLimiterService rateLimiterService;
    private final LoggingService loggingService;
    private final Map<String, Integer> blackList = new HashMap<>();
    AtomicInteger atomicInteger = new AtomicInteger(0); // for hard tests
    @Value("${bucket4j.max-requests-per-ip}")
    private static byte maxManyRequests;
    private static String lastLog = "";

    @Autowired
    public RateLimitingAspect(RateLimiterService rateLimiterService, LoggingService loggingService) {
        this.rateLimiterService = rateLimiterService;
        this.loggingService = loggingService;
    }

    @Before("execution(* ua.com.obox.dbschema..*Controller.*(..)) && @annotation(org.springframework.web.bind.annotation.GetMapping)")
    public synchronized void beforeControllerMethod(JoinPoint joinPoint) {
        HttpServletRequest servletRequest = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        String ipAddress = IPTools.getOriginallyIpFromHeader(servletRequest);
        checkBlockIp(ipAddress);
        logging(servletRequest, ipAddress, joinPoint.getSignature().getName());
        checkBlacklist(ipAddress, "GET");
    }


    @Before("execution(* ua.com.obox.dbschema..*Controller.*(..)) && @annotation(org.springframework.web.bind.annotation.PostMapping)")
    public void beforePostMethod(JoinPoint joinPoint) {
        System.out.println("join post " + atomicInteger.incrementAndGet());
        HttpServletRequest servletRequest = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        String ipAddress = IPTools.getOriginallyIpFromHeader(servletRequest);
        checkBlockIp(ipAddress);
        logging(servletRequest, ipAddress, joinPoint.getSignature().getName());
        checkBlacklist(ipAddress, "POST");
    }

    @Before("execution(* ua.com.obox.dbschema..*Controller.*(..)) && @annotation(org.springframework.web.bind.annotation.PatchMapping)")
    public void beforePatchMethod(JoinPoint joinPoint) {
        HttpServletRequest servletRequest = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        String ipAddress = IPTools.getOriginallyIpFromHeader(servletRequest);
        checkBlockIp(ipAddress);
        logging(servletRequest, ipAddress, joinPoint.getSignature().getName());
        checkBlacklist(ipAddress, "PATCH");
    }

    @Before("execution(* ua.com.obox.dbschema..*Controller.*(..)) && @annotation(org.springframework.web.bind.annotation.DeleteMapping)")
    public void beforeDeleteMethod(JoinPoint joinPoint) {
        HttpServletRequest servletRequest = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        String ipAddress = IPTools.getOriginallyIpFromHeader(servletRequest);
        checkBlockIp(ipAddress);
        logging(servletRequest, ipAddress, joinPoint.getSignature().getName());
        checkBlacklist(ipAddress, "DELETE");
    }

    private void checkBlacklist(String ipAddress, String type) {
        if (ipAddress != null && !rateLimiterService.isAllowedGet(ipAddress, type)) {
            if (!blackList.containsKey(ipAddress)) {
                loggingService.log(LogLevel.CRITICAL, ipAddress, "TOO MANY REQUESTS");
                blackList.put(ipAddress, 1);
            } else {
                blackList.put(ipAddress, blackList.get(ipAddress) + 1);
                System.out.println("count blask " + blackList.get(ipAddress));
            }
            throw new BadFieldsResponse(HttpStatus.TOO_MANY_REQUESTS);
        }
    }

    private void checkBlockIp(String ipAddress) {
        if (blackList.getOrDefault(ipAddress, 0) > maxManyRequests) {
            throw new BadFieldsResponse(HttpStatus.FORBIDDEN);
        }
    }

    private void logging(HttpServletRequest servletRequest, String ipAddress, String methodName) {
        if (!lastLog.equals(String.format("%s %s", methodName, servletRequest.getRequestURI()))) {
            lastLog = String.format("%s %s", methodName, servletRequest.getRequestURI());
            loggingService.log(LogLevel.INFO, ipAddress, String.format("%s %s", methodName, servletRequest.getRequestURI()));
        }
    }
}