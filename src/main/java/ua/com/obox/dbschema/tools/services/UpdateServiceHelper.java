package ua.com.obox.dbschema.tools.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ua.com.obox.dbschema.tools.Validator;
import ua.com.obox.dbschema.tools.configuration.ValidationConfiguration;
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
        if (value == null) {
            setter.accept(null);
            return null;
        }
        if (removeExtraSpaces(value).isEmpty()) {
            setter.accept("");
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

    public String updatePriceField(Consumer<Double> setter, Double value, int maxValue, String fieldName, boolean zeroApply, String acceptLanguage) {
        if (zeroApply && value == 0) {
            setter.accept(null);
            return null;
        } else if (value == null) {
            loggingService.log(LogLevel.ERROR, translation.getString("en-US.priceRequired"));
            return translation.getString(acceptLanguage + ".priceRequired");
        }
        String result = Validator.positiveInteger(fieldName, value, maxValue, acceptLanguage);
        if (result == null && value > 0) {
            setter.accept(value);
        } else {
            {
                return result;
            }
        }
        return null;
    }

    public String updateWeightUnit(Consumer<String> setter, String value, String acceptLanguage) {
        if (value == null) {
            setter.accept(null);
            return null;
        }
        String checkField = Validator.validateWeightUnit(value, acceptLanguage);
        if (checkField == null) {
            setter.accept(value);
        }
        return checkField;
    }

    public String updateState(Consumer<String> setter, String value, String acceptLanguage) {
        String state = Validator.validateState(value, acceptLanguage);
        if (state == null)
            setter.accept(value);
        return state;
    }

    public static String removeExtraSpaces(String str) {
        return str.trim().replaceAll("\\s+", " ");
    }

    public static String removeSpacesAndDuplicateSeparators(String str) {
        return str.replaceAll("\s+", "").replaceAll("/{2,}", "/").replaceAll("^\\/|\\/$", "");
    }

    public String updateAllergens(Consumer<String> setter, String[] allergens, String acceptLanguage) {
        if (allergens[0].isEmpty()) {
            setter.accept(null);
        } else if (String.join(",", allergens).matches(ValidationConfiguration.UUID_REGEX)) {
            setter.accept(String.join(",", allergens));
        } else return translation.getString(acceptLanguage + ".badSortedList");
        return null;
    }
}
