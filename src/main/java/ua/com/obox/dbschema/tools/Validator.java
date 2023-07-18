package ua.com.obox.dbschema.tools;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import ua.com.obox.dbschema.tools.exception.Message;
import ua.com.obox.dbschema.tools.logging.LogLevel;
import ua.com.obox.dbschema.tools.logging.LoggingService;


@RequiredArgsConstructor
public class Validator {
    public static void validateName(String loggingMessage, String name, LoggingService loggingService) {
        name = name.trim(); // delete whitespaces
        if (name.isEmpty()) {
            loggingService.log(LogLevel.ERROR, loggingMessage + Message.ERROR.getMessage() + Message.REQUIRED.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, Message.REQUIRED.getMessage().trim());
        }
        if (name.length() > 200) {
            loggingService.log(LogLevel.ERROR, loggingMessage + Message.ERROR.getMessage() + Message.LIMIT_200.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, Message.LIMIT_200.getMessage().trim());
        }
    }
}
