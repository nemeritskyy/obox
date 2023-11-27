package ua.com.obox.dbschema.tools;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ua.com.obox.dbschema.tools.configuration.ValidationConfiguration;
import ua.com.obox.dbschema.tools.exception.Message;
import ua.com.obox.dbschema.tools.logging.LogLevel;
import ua.com.obox.dbschema.tools.logging.LoggingService;
import ua.com.obox.dbschema.tools.validation.WeightUnits;

import javax.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.util.ResourceBundle;

@RequiredArgsConstructor
@Service
public class Validator {
    private static final ResourceBundle translation = ResourceBundle.getBundle("translation.messages");
    private final LoggingService loggingService;
    private static LoggingService staticLoggingService;

    @PostConstruct
    private void init() {
        staticLoggingService = this.loggingService;
    }

    public static String validateNameTranslationSupport(String name, String acceptLanguage) {
        if (name == null || name.trim().isEmpty()) {
            staticLoggingService.log(LogLevel.ERROR,
                    translation.getString("en-US.nameRequired")
            );
            return translation.getString(acceptLanguage + ".nameRequired");
        }
        name = removeExtraSpaces(name);
        if (name.length() > 200) {
            staticLoggingService.log(LogLevel.ERROR,
                    translation.getString("en-US.nameLimit")
            );
            return translation.getString(acceptLanguage + ".nameLimit");
        }
        return null;
    }

    public static String validateVarcharTranslationSupport(String str, String fieldName, String acceptLanguage) {
        if (str != null && str.trim().isEmpty() || str != null && removeExtraSpaces(str).length() > 255) {
            staticLoggingService.log(LogLevel.ERROR,
                    String.format(translation.getString("en-US.varcharLimit"), translation.getString("en-US." + fieldName))
            );
            return String.format(translation.getString(acceptLanguage + ".varcharLimit"), translation.getString(acceptLanguage + "." + fieldName));
        }
        return null;
    }

    public static String validateWeightUnit(String str, String acceptLanguage) {
        if (!isValidWeightUnit(str)) {
            staticLoggingService.log(LogLevel.ERROR,
                    translation.getString("en-US.weightUnit")
            );
            return translation.getString(acceptLanguage + ".weightUnit");
        }
        return null;
    }

    private static boolean isValidWeightUnit(String str) {
        try {
            WeightUnits.valueOf(str.toUpperCase());
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    public static String validateWeight(String str, String acceptLanguage) {
        System.out.println(str);
        if (!str.matches(ValidationConfiguration.WEIGHT_REGEX)) {
            staticLoggingService.log(LogLevel.ERROR,
                    translation.getString("en-US.weight")
            );
            return translation.getString(acceptLanguage + ".weight");
        }
        for (String weight : str.split("/")) {
            if (Long.parseLong(weight) > ValidationConfiguration.MAX_WEIGHT) {
                staticLoggingService.log(LogLevel.ERROR, String.format(translation.getString("en-US.weightLimit"), ValidationConfiguration.MAX_WEIGHT));
                return String.format(translation.getString(acceptLanguage + ".weightLimit"), ValidationConfiguration.MAX_WEIGHT);
            }
        }
        return null;
    }

    public static String positiveInteger(String field, Number inputInteger, int maxInteger, String acceptLanguage) {
        if (inputInteger != null && inputInteger.doubleValue() <= 0 || inputInteger != null && inputInteger.doubleValue() > maxInteger) {
            staticLoggingService.log(LogLevel.ERROR, String.format(translation.getString("en-US.integerRange"), translation.getString("en-US." + field), maxInteger));
            return String.format(translation.getString(acceptLanguage + ".integerRange"), translation.getString(acceptLanguage + "." + field), maxInteger);
        }
        return null;
    }

    public static String validateState(String state, String acceptLanguage) {
        if (state == null || (!state.equals(State.ENABLED) && !state.equals(State.DISABLED))) {
            staticLoggingService.log(LogLevel.ERROR, translation.getString("en-US.state"));
            return String.format(translation.getString(acceptLanguage + ".state"));
        }
        return null;
    }

    public static String validateLanguage(String language, String acceptLanguage) {
        if (language == null || !language.matches(ValidationConfiguration.LANGUAGE_REGEX)) {
            staticLoggingService.log(LogLevel.ERROR,
                    translation.getString("en-US.language")
            );
            return translation.getString(acceptLanguage + ".language");
        }
        if (!ValidationConfiguration.SUPPORT_LANGUAGES.contains(language))
            return translation.getString(acceptLanguage + ".languageNotSupport");
        return null;
    }

    public static String detectImageType(byte[] imageData, LoggingService loggingService) {
        if (imageData.length >= 8) {
            byte[] jpegSignature = {(byte) 0xFF, (byte) 0xD8};
            byte[] pngSignature = {(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A};

            if (startsWith(imageData, jpegSignature)) {
                return ".jpg";
            } else if (startsWith(imageData, pngSignature)) {
                return ".png";
            }

            String svgContent = new String(imageData, StandardCharsets.UTF_8);
            if (svgContent.trim().startsWith("<?xml")) {
                return ".svg";
            }

            if (imageData[4] == 0x66 && imageData[5] == 0x74 && imageData[6] == 0x79 && imageData[7] == 0x70) {
                return ".heic";
            }
        }

        loggingService.log(LogLevel.ERROR, Message.BAD_IMAGE_TYPE.getMessage());
        return null;
    }


    private static boolean startsWith(byte[] array, byte[] prefix) {
        if (array.length < prefix.length) {
            return false;
        }
        for (int i = 0; i < prefix.length; i++) {
            if (array[i] != prefix[i]) {
                return false;
            }
        }
        return true;
    }

    public static String removeExtraSpaces(String str) {
        if (str != null)
            return str.trim().replaceAll("\\s+", " ");
        return null;
    }
}
