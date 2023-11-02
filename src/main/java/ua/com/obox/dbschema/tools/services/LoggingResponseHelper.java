package ua.com.obox.dbschema.tools.services;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import ua.com.obox.dbschema.tools.logging.LogLevel;
import ua.com.obox.dbschema.tools.logging.LoggingService;

public class LoggingResponseHelper {
    public static void loggingThrowException(
                                             LogLevel logLevel,
                                             HttpStatus httpStatus,
                                             String translationMessage,
                                             String loggingMessage,
                                             LoggingService loggingService) {
        loggingService.log(logLevel, loggingMessage);
        throw new ResponseStatusException(httpStatus, translationMessage);
    }
}