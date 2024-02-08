package ua.com.obox.dbschema.tools.logging;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import java.util.Date;

@Component
public class LogToFile extends OncePerRequestFilter  {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        if (request.getAttribute("LogToFileExecuted") == null) {
            LoggingService.addRecordToLog(
                    LogEntry.builder()
                            .level(LogLevel.INFO)
                            .ip(IPTools.getOriginallyIpFromHeader(request))
                            .message(String.format("%s %s", request.getMethod(), request.getServletPath()))
                            .serverTime(new Date())
                            .unixTime(Instant.now().getEpochSecond())
                            .build().toString());
            request.setAttribute("LogToFileExecuted", true);
        }
        filterChain.doFilter(request, response);
    }
}
