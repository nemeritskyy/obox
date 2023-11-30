package ua.com.obox.dbschema.tools.logging;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class LoggingService {
    private final LogRepository logRepository;

    public void log(LogLevel level, String message) {
        LogEntry logEntry = new LogEntry(null, level, null, message, new Date(), Instant.now().getEpochSecond());
        try {
            logRepository.save(logEntry);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void log(LogLevel level, HttpServletRequest servletRequest, String message) {
        LogEntry logEntry = new LogEntry(null, level, IPTools.getOriginallyIpFromHeader(servletRequest), message, new Date(), Instant.now().getEpochSecond());
        try {
            logRepository.save(logEntry);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

