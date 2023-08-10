package ua.com.obox.dbschema.tools.services;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import ua.com.obox.dbschema.tools.logging.LogLevel;
import ua.com.obox.dbschema.tools.logging.LoggingService;

public class LoggingResponseHelper {
    public static void loggingThrowException(String entityId,
                                             LogLevel logLevel,
                                             HttpStatus httpStatus,
                                             String loggingMessage,
                                             String responseMessage,
                                             LoggingService loggingService) {
        loggingService.log(logLevel, loggingMessage + " param: " + entityId + " " + httpStatus.toString());
        throw new ResponseStatusException(httpStatus, responseMessage);
    }
}