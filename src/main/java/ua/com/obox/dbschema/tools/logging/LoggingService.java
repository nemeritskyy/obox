package ua.com.obox.dbschema.tools.logging;

import lombok.RequiredArgsConstructor;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class LoggingService implements EnvironmentAware {

    private final LogRepository logRepository;
    public static String logPath;

    @Override
    public void setEnvironment(Environment environment) {
        logPath = environment.getProperty("application.log-path");
    }

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

    public void log(LogLevel level, String ip, String message) {
        LogEntry logEntry = new LogEntry(null, level, ip, message, new Date(), Instant.now().getEpochSecond());
        try {
            logRepository.save(logEntry);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void addRecordToLog(String logging) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        String todayDate = dateFormat.format(new Date());

        String logFilePath = logPath + File.separator + todayDate + ".log";
        File logFile = new File(logFilePath);
        try (FileWriter fileWriter = new FileWriter(logFile, true)) {
            fileWriter.write(logging);
            System.out.print(logging);
        } catch (IOException e) {
            System.out.printf("Error writing log entry to file: {%s %s}", logFilePath, e);
        }
    }
}

