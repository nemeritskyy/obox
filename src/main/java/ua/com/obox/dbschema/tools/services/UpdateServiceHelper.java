package ua.com.obox.dbschema.tools.services;

import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ua.com.obox.authserver.confirmation.Confirm;
import ua.com.obox.authserver.confirmation.ConfirmRepository;
import ua.com.obox.authserver.mail.EmailService;
import ua.com.obox.authserver.user.User;
import ua.com.obox.dbschema.dish.Dish;
import ua.com.obox.dbschema.tools.Validator;
import ua.com.obox.dbschema.tools.configuration.ValidationConfiguration;
import ua.com.obox.dbschema.tools.logging.LogLevel;
import ua.com.obox.dbschema.tools.logging.LoggingService;

import java.util.Objects;
import java.util.Optional;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.function.Consumer;
import java.util.regex.Pattern;

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

    public String updateWeightField(Dish dish, String value, String acceptLanguage) {
        if (value == null || removeSpacesAndDuplicateSeparators(value).isEmpty()) {
            dish.setWeight(null);
            return null;
        }
        String checkField = Validator.validateWeight(removeSpacesAndDuplicateSeparators(value), acceptLanguage);
        if (checkField == null) {
            dish.setWeight(removeSpacesAndDuplicateSeparators(value));
        }
        return checkField;
    }

    public String updateWeightUnit(Dish dish, String value, String acceptLanguage) {
        if (value == null || value.equals("")) {
            dish.setWeightUnit(null);
            return null;
        }
        String checkField = Validator.validateWeightUnit(value, acceptLanguage);
        if (checkField == null) {
            dish.setWeightUnit(value.toUpperCase());
        }
        return checkField;
    }

    public String checkWeight(Dish dish, Dish request, Map<String, String> fieldErrors, String acceptLanguage) {
        if (dish.getWeight() == null && dish.getWeightUnit() != null) {
            if (!fieldErrors.containsKey("weight"))
                fieldErrors.put("weight", translation.getString(acceptLanguage + ".weightRequired"));
        }
        if (dish.getWeightUnit() == null && (dish.getWeight() != null || request.getWeight() != null)) {
            if (!Objects.equals(request.getWeight(), "")) {
                fieldErrors.put("weight_unit", translation.getString(acceptLanguage + ".weightUnitRequired"));
            }
        }
        return null;
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
        return str.replaceAll("\s+", "").replaceAll("/{2,}", "/").replaceAll("^/|/$", "");
    }

    public String updateAllergens(Consumer<String> setter, String[] allergens, String acceptLanguage) {
        if (allergens != null) {
            if (allergens[0].isEmpty()) {
                setter.accept(null);
            } else if (String.join(",", allergens).matches(ValidationConfiguration.UUID_REGEX)) {
                setter.accept(String.join(",", allergens));
            } else return translation.getString(acceptLanguage + ".badSortedList");
        }
        return null;
    }

    public String checkExistUser(Optional<User> userExist, String errorLanguage) {
        if (userExist.isPresent()) {
            return translation.getString(errorLanguage + ".userExist");
        } else return null;
    }

    public String checkEmail(String email, String errorLanguage) {
        Pattern pattern = Pattern.compile("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$");
        if (!pattern.matcher(email).find()) {
            return translation.getString(errorLanguage + ".userBadEmail");
        } else return null;
    }

    public String checkPassword(String password, String errorLanguage) {
        Pattern pattern = Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$");
        if (!pattern.matcher(password).find()) {
            return translation.getString(errorLanguage + ".badPassword");
        } else return null;
    }

    public String checkActivate(Optional<User> userExist, String errorLanguage, String email, ConfirmRepository confirmRepository, EmailService emailService) {
        if (userExist.isPresent()) {

            Optional<Confirm> alreadyExist = confirmRepository.findByEmail(email);
            alreadyExist.ifPresent(confirmToken -> {
                String newToken = RandomStringUtils.random(20, true, true);
                confirmRepository.delete(alreadyExist.get());
                Confirm confirm = Confirm.builder().confirmationKey(newToken).email(email).build();
                confirmRepository.save(confirm);
                emailService.sendEmailConfirmation(confirm.getEmail(), confirm.getConfirmationKey());
            });

            if (!userExist.get().isEnabled()) {
                return translation.getString(errorLanguage + ".userActivation");
            }
        }
        return null;
    }

    public String updateColorHex(Consumer<String> setColorHex, String color, String errorLanguage) {
        Pattern pattern = Pattern.compile("^#(?:[0-9a-fA-F]{3}){1,2}$");
        if (color != null && pattern.matcher(color).find()) {
            setColorHex.accept(color);
            return null;
        } else return translation.getString(errorLanguage + ".badColorHex");
    }

    public String updateEmoji(Consumer<String> setEmoji, String emoji, String errorLanguage) {
        if (emoji == null) {
            setEmoji.accept(null);
            return null;
        }
        if (removeExtraSpaces(emoji).isEmpty()) {
            setEmoji.accept(null);
            return null;
        }
        String checkField = Validator.validateEmoji(emoji, errorLanguage);
        if (checkField == null) {
            setEmoji.accept(removeExtraSpaces(emoji));
        }
        return checkField;
    }
}