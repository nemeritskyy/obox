package ua.com.obox.dbschema.tools.logging;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

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