package ua.com.obox.dbschema.tools.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ua.com.obox.dbschema.tools.Validator;
import ua.com.obox.dbschema.tools.logging.LogLevel;
import ua.com.obox.dbschema.tools.logging.LoggingService;

import java.util.ResourceBundle;
import java.util.function.Consumer;

@Service
public class UpdateServiceHelper {
    @Autowired
    LoggingService loggingService;

    private final ResourceBundle translation = ResourceBundle.getBundle("translation.messages");

    public String updateNameField(Consumer<String> setter, String value, String acceptLanguage) {
        String name = Validator.validateNameTranslationSupport(value, acceptLanguage);
        if (name == null)
            setter.accept(removeExtraSpaces(value));
        return name;
    }

    public String updateVarcharField(Consumer<String> setter, String value, String fieldName, String acceptLanguage) {
        if (value == null || removeExtraSpaces(value).isEmpty()) {
            setter.accept(null);
            return null;
        }
        String checkField = Validator.validateVarcharTranslationSupport(value, fieldName, acceptLanguage);
        if (checkField == null) {
            setter.accept(removeExtraSpaces(value));
        }
        return checkField;
    }

    public String updateWeightField(Consumer<String> setter, String value, String acceptLanguage) {
        if (value == null || removeSpacesAndDuplicateSeparators(value).isEmpty()) {
            setter.accept(null);
            return null;
        }
        String checkField = Validator.validateWeight(removeSpacesAndDuplicateSeparators(value), acceptLanguage);
        if (checkField == null) {
            setter.accept(removeSpacesAndDuplicateSeparators(value));
        }
        return checkField;
    }

    public String updateIntegerField(Consumer<Integer> setter, Integer value, int maxValue, String fieldName, String acceptLanguage) {
        if (value != null) {
            if (value == 0) {
                setter.accept(null);
            } else {
                String result = Validator.positiveInteger(fieldName, value, maxValue, acceptLanguage);
                if (result == null) {
                    setter.accept(value);
                }
                return result;
            }
        }
        return null;
    }

    public String updatePriceField(Consumer<Double> setter, Double value, int maxValue, String fieldName, String acceptLanguage) {
        if (value != null) {
            if (value == 0) {
                loggingService.log(LogLevel.ERROR, translation.getString("en-US.priceRequired"));
                return translation.getString(acceptLanguage + ".priceRequired");
            }
            String result = Validator.positiveInteger(fieldName, value, maxValue, acceptLanguage);
            if (result == null) {
                setter.accept(value);
            }
            return result;
        } else {
            loggingService.log(LogLevel.ERROR, translation.getString("en-US.priceRequired"));
            return translation.getString(acceptLanguage + ".priceRequired");
        }
    }

    public String updateWeightUnit(Consumer<String> setter, String value, String acceptLanguage) {
        if (value == null || removeExtraSpaces(value).isEmpty()) {
            setter.accept(null);
            return null;
        }
        String checkField = Validator.validateWeightUnit(value, acceptLanguage);
        if (checkField == null) {
            setter.accept(removeExtraSpaces(value));
        }
        return checkField;
    }

    public String updateState(Consumer<String> setter, String value, String acceptLanguage) {
        String state = Validator.validateState(value, acceptLanguage);
        if (state == null)
            setter.accept(value);
        return state;
    }

    public String updateLanguageCode(String value, String acceptLanguage) {
        return Validator.languageCode("createMenu", value, acceptLanguage);
    }

    public static String removeExtraSpaces(String str) {
        return str.trim().replaceAll("\\s+", " ");
    }

    public static String removeSpacesAndDuplicateSeparators(String str) {
        return str.replaceAll("\s+", "").replaceAll("/{2,}", "/").replaceAll("^\\/|\\/$", "");
    }
}
