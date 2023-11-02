package ua.com.obox.dbschema.tools.exception;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import ua.com.obox.dbschema.tools.logging.LogLevel;
import ua.com.obox.dbschema.tools.logging.LoggingService;
import ua.com.obox.dbschema.tools.services.LoggingResponseHelper;

import javax.annotation.PostConstruct;
import java.util.ResourceBundle;

@RequiredArgsConstructor
@Service
public class ExceptionTools {
    private static final ResourceBundle translation = ResourceBundle.getBundle("translation.messages");
    private final LoggingService loggingService;
    private static LoggingService staticLoggingService;

    @PostConstruct
    private void init() {
        staticLoggingService = this.loggingService;
    }

    public static void notFoundResponse(String translationMessage, String acceptLanguage, String entityId) {
        LoggingResponseHelper.loggingThrowException(
                LogLevel.ERROR, HttpStatus.NOT_FOUND,
                String.format(translation.getString(acceptLanguage + translationMessage), entityId),
                String.format(translation.getString("en-US" + translationMessage), entityId),
                staticLoggingService);
    }

    public static void forbiddenResponse(String acceptLanguage, String entityId) {
        LoggingResponseHelper.loggingThrowException(
                LogLevel.ERROR, HttpStatus.FORBIDDEN,
                String.format(translation.getString(acceptLanguage + ".tenantForbidden"), entityId),
                String.format(translation.getString("en-US.tenantForbidden"), entityId),
                staticLoggingService);
    }
}
