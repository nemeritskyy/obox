package ua.com.obox.dbschema.tools.services;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import ua.com.obox.dbschema.tools.Validator;
import ua.com.obox.dbschema.tools.logging.LogLevel;
import ua.com.obox.dbschema.tools.logging.LoggingService;

import java.util.function.Consumer;

@Service
public class UpdateServiceHelper {
    public void updateVarcharField(Consumer<String> setter, String value, String field, String loggingMessage, LoggingService loggingService) {
        if (value == null || value.trim().isEmpty()) {
            setter.accept(null);
        } else {
            Validator.validateVarchar(loggingMessage, field, value, loggingService);
            setter.accept(value.trim());
        }
    }

    public void updateNameField(Consumer<String> setter, String value, String field, String loggingMessage, LoggingService loggingService) {
        Validator.validateName(loggingMessage, value, loggingService);
        setter.accept(value.trim());
    }

    public void updateIntegerField(Consumer<Integer> setter, Integer value, String field, String loggingMessage, LoggingService loggingService, int maxValue) {
        if (value != null) {
            if (value == 0) {
                setter.accept(null);
            } else {
                Validator.positiveInteger(field, value, maxValue, loggingService);
                setter.accept(value);
            }
        }
    }

    public void updatePriceField(Consumer<Double> setter, Double value, String field, String loggingMessage, LoggingService loggingService, int maxValue) {
        if (value != null) {
            Validator.positiveInteger(field, value, maxValue, loggingService);
            setter.accept(value);
        } else {
            loggingService.log(LogLevel.ERROR, loggingMessage + " The price cannot be an empty");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The price cannot be an empty");
        }
    }

    public void updateState(Consumer<String> setter, String value, String field, String loggingMessage, LoggingService loggingService) {
        Validator.validateState(loggingMessage, value, loggingService);
        setter.accept(value);
    }
}
