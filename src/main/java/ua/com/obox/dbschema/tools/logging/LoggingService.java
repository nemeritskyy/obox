package ua.com.obox.dbschema.tools.logging;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LoggingService {
    private final LogRepository logRepository;

    public void log(LogLevel level, String message) {
        LogEntry logEntry = new LogEntry(level, message);
        try {
            logRepository.save(logEntry);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

