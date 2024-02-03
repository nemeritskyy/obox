package ua.com.obox.dbschema.tools.logging;

import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import java.util.Date;

@Component
public class LogToFile extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        HttpServletRequest servletRequest = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        LoggingService.addRecordToLog(
                LogEntry.builder()
                        .level(LogLevel.INFO)
                        .ip(IPTools.getOriginallyIpFromHeader(servletRequest))
                        .message(String.format("%s %s", servletRequest.getMethod(), servletRequest.getServletPath()))
                        .serverTime(new Date())
                        .unixTime(Instant.now().getEpochSecond())
                        .build().toString());

        filterChain.doFilter(request, response);
    }


}
