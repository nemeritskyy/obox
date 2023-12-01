package ua.com.obox.security.bucket4j;

import org.aspectj.lang.JoinPoint;
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
    public synchronized void beforeControllerMethod(JoinPoint joinPoint) {
        System.out.println("join get " + atomicInteger.incrementAndGet());
        HttpServletRequest servletRequest = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        String ipAddress = IPTools.getOriginallyIpFromHeader(servletRequest);
        logging(servletRequest, ipAddress, joinPoint.getSignature().getName());

        if (ipAddress != null && !rateLimiterService.isAllowedGet(ipAddress, "GET")) {
            if (!blackList.contains(ipAddress)) {
                loggingService.log(LogLevel.CRITICAL, ipAddress, "TOO MANY REQUESTS");
                blackList.add(ipAddress);
            }
            throw new BadFieldsResponse(HttpStatus.TOO_MANY_REQUESTS);
        }
    }

    @Before("execution(* ua.com.obox.dbschema..*Controller.*(..)) && @annotation(org.springframework.web.bind.annotation.PostMapping)")
    public void beforePostMethod(JoinPoint joinPoint) { // post limits
        System.out.println("join post " + atomicInteger.incrementAndGet());
        HttpServletRequest servletRequest = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        String ipAddress = IPTools.getOriginallyIpFromHeader(servletRequest);
        logging(servletRequest, ipAddress, joinPoint.getSignature().getName());

        if (ipAddress != null && !rateLimiterService.isAllowedPost(ipAddress, "POST")) {
            if (!blackList.contains(ipAddress)) {
                loggingService.log(LogLevel.CRITICAL, ipAddress, "TOO MANY REQUESTS");
                blackList.add(ipAddress);
            }
            throw new BadFieldsResponse(HttpStatus.TOO_MANY_REQUESTS);
        }
    }

    @Before("execution(* ua.com.obox.dbschema..*Controller.*(..)) && @annotation(org.springframework.web.bind.annotation.PatchMapping)")
    public void beforePatchMethod(JoinPoint joinPoint) { // post limits
        System.out.println("join patch " + atomicInteger.incrementAndGet());
        HttpServletRequest servletRequest = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        String ipAddress = IPTools.getOriginallyIpFromHeader(servletRequest);
        logging(servletRequest, ipAddress, joinPoint.getSignature().getName());

        if (ipAddress != null && !rateLimiterService.isAllowedPatch(ipAddress, "PATCH")) {
            if (!blackList.contains(ipAddress)) {
                loggingService.log(LogLevel.CRITICAL, ipAddress, "TOO MANY REQUESTS");
                blackList.add(ipAddress);
            }
            throw new BadFieldsResponse(HttpStatus.TOO_MANY_REQUESTS);
        }
    }

    @Before("execution(* ua.com.obox.dbschema..*Controller.*(..)) && @annotation(org.springframework.web.bind.annotation.DeleteMapping)")
    public void beforeDeleteMethod(JoinPoint joinPoint) { // post limits
        System.out.println("join delete " + atomicInteger.incrementAndGet());
        HttpServletRequest servletRequest = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        String ipAddress = IPTools.getOriginallyIpFromHeader(servletRequest);
        logging(servletRequest, ipAddress, joinPoint.getSignature().getName());

        if (ipAddress != null && !rateLimiterService.isAllowedDelete(ipAddress, "DELETE")) {
            if (!blackList.contains(ipAddress)) {
                loggingService.log(LogLevel.CRITICAL, ipAddress, "TOO MANY REQUESTS");
                blackList.add(ipAddress);
            }
            throw new BadFieldsResponse(HttpStatus.TOO_MANY_REQUESTS);
        }
    }

    private void logging(HttpServletRequest servletRequest, String ipAddress, String methodName) {
        loggingService.log(LogLevel.INFO, ipAddress, String.format("%s %s", methodName, servletRequest.getRequestURI()));
    }
}