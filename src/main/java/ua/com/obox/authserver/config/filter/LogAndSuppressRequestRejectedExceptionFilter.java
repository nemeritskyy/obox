package ua.com.obox.authserver.config.filter;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.GenericFilterBean;
import ua.com.obox.dbschema.tools.ip.IPBlackList;
import ua.com.obox.dbschema.tools.logging.IPTools;
import ua.com.obox.dbschema.tools.logging.LogEntry;
import ua.com.obox.dbschema.tools.logging.LogLevel;
import ua.com.obox.dbschema.tools.logging.LoggingService;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import java.util.Date;

@Component
@RequiredArgsConstructor
public class LogAndSuppressRequestRejectedExceptionFilter extends GenericFilterBean {
    private final IPBlackList ipTools;

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;
        String ipAddress = IPTools.getOriginallyIpFromHeader(request);
        String url = request.getRequestURI();
        if (!ipTools.checkBlackList(ipAddress)) {
            boolean containsWarmRequest = ipTools.warmRequests.stream().anyMatch(url::contains);
            if (containsWarmRequest) {
                ipTools.addToBlackList(ipAddress, url);

                LoggingService.addRecordToLog(
                        LogEntry.builder()
                                .level(LogLevel.INFO)
                                .ip(ipAddress)
                                .message(String.format("agent={%s} url={%s}", request.getHeader(HttpHeaders.USER_AGENT),
                                        url))
                                .serverTime(new Date())
                                .unixTime(Instant.now().getEpochSecond())
                                .build().toString());
                response.sendError(HttpServletResponse.SC_FORBIDDEN);
            } else {
                chain.doFilter(req, res);
            }
        } else {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
        }
    }
}
