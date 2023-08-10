package ua.com.obox.dbschema.restaurant;

import org.springframework.stereotype.Service;
import ua.com.obox.dbschema.tools.Validator;
import ua.com.obox.dbschema.tools.logging.LoggingService;

import java.util.function.Consumer;

@Service
public class RestaurantServiceHelper {
    public void updateVarcharField(Consumer<String> setter, String value, String field, String loggingMessage, LoggingService loggingService) {
        if (value != null) {
            String trimmedValue = value.trim();
            if (!trimmedValue.isEmpty()) {
                Validator.validateVarchar(loggingMessage, field, trimmedValue, loggingService);
                setter.accept(trimmedValue);
            } else {
                setter.accept(null);
            }
        }
    }

    public void updateNameField(Consumer<String> setter, String value, String field, String loggingMessage, LoggingService loggingService) {
        if (value != null) {
            Validator.validateName(loggingMessage, value, loggingService);
            setter.accept(value.trim());
        }
    }
}
