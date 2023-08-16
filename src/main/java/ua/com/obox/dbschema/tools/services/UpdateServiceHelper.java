package ua.com.obox.dbschema.tools.services;

import org.springframework.stereotype.Service;
import ua.com.obox.dbschema.tools.Validator;
import ua.com.obox.dbschema.tools.logging.LogLevel;
import ua.com.obox.dbschema.tools.logging.LoggingService;

import java.util.function.Consumer;

@Service
public class UpdateServiceHelper {
    public String updateVarcharField(Consumer<String> setter, String value, String field, String loggingMessage, LoggingService loggingService) {
        if (value == null || value.trim().isEmpty()) {
            setter.accept(null);
            return null;
        }

        String checkField = Validator.validateVarchar(loggingMessage, field, value, loggingService);
        if (checkField == null) {
            setter.accept(value);
        }
        return checkField;
    }

    public String updateNameField(Consumer<String> setter, String value, String field, String loggingMessage, LoggingService loggingService) {
        String name = Validator.validateName(loggingMessage, value, loggingService);
        if (name == null)
            setter.accept(value.trim());
        return name;
    }

    public String updateIntegerField(Consumer<Integer> setter, Integer value, String field, String loggingMessage, LoggingService loggingService, int maxValue) {
        if (value != null) {
            if (value == 0) {
                setter.accept(null);
            } else {
                String result = Validator.positiveInteger(field, value, maxValue, loggingService);
                if (result == null) {
                    setter.accept(value);
                }
                return result;
            }
        }
        return null;
    }

    public String updatePriceField(Consumer<Double> setter, Double value, String field, String loggingMessage, LoggingService loggingService, int maxValue) {
        if (value != null) {
            String result = Validator.positiveInteger(field, value, maxValue, loggingService);
            if (result == null) {
                setter.accept(value);
            }
            return result;
        } else {
            loggingService.log(LogLevel.ERROR, loggingMessage + " The price cannot be an empty");
            return "The price cannot be an empty";
        }
    }

    public String updateState(Consumer<String> setter, String value, String field, String loggingMessage, LoggingService loggingService) {
        String state = Validator.validateState(loggingMessage, value, loggingService);
        if (state == null)
            setter.accept(value);
        return state;
    }

    public String updateLanguageCode(Consumer<String> setter, String value, String field, String loggingMessage, LoggingService loggingService) {
        String languageCode = Validator.languageCode("createMenu", value, loggingService);
        return languageCode;
    }
}
