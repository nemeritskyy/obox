package ua.com.obox.security.bucket4j;

import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import ua.com.obox.authserver.token.TokenRepository;
import ua.com.obox.dbschema.dish.DishService;
import ua.com.obox.dbschema.tools.logging.IPTools;
import ua.com.obox.dbschema.tools.logging.LogLevel;
import ua.com.obox.dbschema.tools.logging.LoggingService;
import ua.com.obox.dbschema.tools.response.BadFieldsResponse;
import ua.com.obox.security.notification.SendMessage;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Aspect
@Component
@Profile("!test")
@RequiredArgsConstructor
public class RateLimitingAspect {

    private final RateLimiterService rateLimiterService;
    private final LoggingService loggingService;
    private final DishService dishService;
    private final TokenRepository tokenRepository;
    public static Map<String, AtomicInteger> blackList = new HashMap<>();
    private static String lastLog = "";

    @Before("execution(* ua.com.obox.dbschema..*Controller.*(..)) && @annotation(org.springframework.web.bind.annotation.GetMapping)")
    public synchronized void beforeControllerMethod(JoinPoint joinPoint) {
        allChecks(joinPoint, HttpMethod.GET);
    }


    @Before("execution(* ua.com.obox.dbschema..*Controller.*(..)) && @annotation(org.springframework.web.bind.annotation.PostMapping)")
    public void beforePostMethod(JoinPoint joinPoint) {
        allChecks(joinPoint, HttpMethod.POST);
    }

    @Before("execution(* ua.com.obox.dbschema..*Controller.*(..)) && @annotation(org.springframework.web.bind.annotation.PatchMapping)")
    public void beforePatchMethod(JoinPoint joinPoint) {
        allChecks(joinPoint, HttpMethod.PATCH);
    }

    @Before("execution(* ua.com.obox.dbschema..*Controller.*(..)) && @annotation(org.springframework.web.bind.annotation.DeleteMapping)")
    public void beforeDeleteMethod(JoinPoint joinPoint) {
        allChecks(joinPoint, HttpMethod.DELETE);
    }

    private void allChecks(JoinPoint joinPoint, HttpMethod type) {
        HttpServletRequest servletRequest = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        String ipAddress = IPTools.getOriginallyIpFromHeader(servletRequest);
        if (type.equals(HttpMethod.GET)) {
//            checkJWT(servletRequest);
        }
        checkBlockIp(ipAddress, servletRequest);
        logging(servletRequest, ipAddress, joinPoint.getSignature().getName());
        checkBlacklist(ipAddress, type);
    }

    private void checkJWT(HttpServletRequest servletRequest) {
        System.out.println("url " + servletRequest.getRequestURI());
        final String authHeader = servletRequest.getHeader("Authorization");
        final String jwt;
        String url = servletRequest.getRequestURI();
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            System.out.println("Without JWT"); // need login
            return;
        }
        jwt = authHeader.substring(7);
        var storedToken = tokenRepository.findByToken(jwt)
                .orElse(null);
        if (storedToken != null) {
            System.out.println("user id : " + storedToken.user.getUserId());
            if (servletRequest.getRequestURI().startsWith("/dishes")) {
                Pattern pattern = Pattern.compile("/dishes/([a-fA-F0-9\\\\-]+)");
                Matcher matcher = pattern.matcher(url);
                if (matcher.find()){
                    System.out.println("dish id" + matcher.group(1));
                    System.out.println("tenant id" + storedToken.user.getUserId());
                    System.out.println(dishService.isDishInTenant(matcher.group(1), storedToken.user.getUserId()));
                }
            }
        }

    }

    private void checkBlacklist(String ipAddress, HttpMethod type) {
        if (ipAddress != null && !rateLimiterService.isAllowed(ipAddress, type)) {
            if (!blackList.containsKey(ipAddress)) {
                loggingService.log(LogLevel.CRITICAL, ipAddress, "TOO MANY REQUESTS");
                SendMessage.sendToTelegram(String.format("\u26A0 IP:%s TOO MANY REQUESTS", ipAddress));
                blackList.put(ipAddress, new AtomicInteger(1));
            } else {
                blackList.get(ipAddress).incrementAndGet();
            }
            throw new BadFieldsResponse(HttpStatus.TOO_MANY_REQUESTS);
        }
    }

    private void checkBlockIp(String ipAddress, HttpServletRequest servletRequest) {
        AtomicInteger totalManyRequestsFromIp = blackList.getOrDefault(ipAddress, new AtomicInteger(0));
        if (totalManyRequestsFromIp.get() == 10) {
            SendMessage.sendToTelegram(String.format("\uD83D\uDEA8\uD83D\uDEA8\uD83D\uDEA8 IP:%s BLOCKED,\nLAST REQUEST:%s,\nTO UNBLOCK WRITE:\n/unblock%s", ipAddress, servletRequest.getRequestURI(), ipAddress));
        }
        if (totalManyRequestsFromIp.get() >= 10) {
            totalManyRequestsFromIp.incrementAndGet();
            throw new BadFieldsResponse(HttpStatus.FORBIDDEN);
        }
    }

    private void logging(HttpServletRequest servletRequest, String ipAddress, String methodName) {
        String url = servletRequest.getRequestURI();
        if (!lastLog.equals(String.format("%s %s", methodName, url))) {
            lastLog = String.format("%s %s", methodName, url);
            loggingService.log(LogLevel.INFO, ipAddress, String.format("%s %s", methodName, url));
        }
    }
}