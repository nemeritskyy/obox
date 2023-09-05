package ua.com.obox.dbschema.tools;

import lombok.RequiredArgsConstructor;
import ua.com.obox.dbschema.tools.exception.Message;
import ua.com.obox.dbschema.tools.logging.LogLevel;
import ua.com.obox.dbschema.tools.logging.LoggingService;

import java.util.Base64;


@RequiredArgsConstructor
public class Validator {
    public static String validateName(String loggingMessage, String name, LoggingService loggingService) {
        if (name == null || name.trim().isEmpty()) {
            loggingService.log(LogLevel.ERROR, String.format("%s %s %s", loggingMessage, Message.ERROR.getMessage(), Message.REQUIRED.getMessage()));
            return Message.REQUIRED.getMessage();
        }
        name = name.trim().replaceAll("\\s+", " "); // delete whitespaces
        if (name.length() > 200) {
            loggingService.log(LogLevel.ERROR, String.format("%s %s %s", loggingMessage, Message.ERROR.getMessage(), Message.LIMIT_200.getMessage()));
            return Message.LIMIT_200.getMessage();
        }
        return null;
    }

    public static String validateVarchar(String loggingMessage, String fieldName, String str, LoggingService loggingService) {
        if (str != null && str.trim().length() == 0 || str != null && str.length() > 255) {
            loggingService.log(LogLevel.ERROR, String.format("%s %s %s", loggingMessage, Message.ERROR.getMessage(), Message.LIMIT_255.getMessage()));
            return String.format("%s %s", fieldName, Message.LIMIT_255.getMessage());
        }
        return null;
    }

    public static String languageCode(String loggingMessage, String code, LoggingService loggingService) {
        if (code == null || code.length() < 2 || code.length() > 3) {
            loggingService.log(LogLevel.ERROR, String.format("%s %s", loggingMessage, Message.LANGUAGE.getMessage()));
            return Message.LANGUAGE.getMessage();
        }
        return null;
    }

    public static String positiveInteger(String field, Number inputInteger, int maxInteger, LoggingService loggingService) {
        if (inputInteger != null && inputInteger.doubleValue() < 0 || inputInteger != null && inputInteger.doubleValue() > maxInteger) {
            String response = String.format("%s %s %d", field, Message.LIMIT.getMessage(), maxInteger);
            loggingService.log(LogLevel.ERROR, response);
            return response;
        }
        return null;
    }

    public static String validateState(String loggingMessage, String state, LoggingService loggingService) {
        if (state == null || (!state.equals(State.ENABLED) && !state.equals(State.DISABLED))) {
            loggingService.log(LogLevel.ERROR, String.format("%s %s", loggingMessage, Message.BAD_STATE.getMessage()));
            return Message.BAD_STATE.getMessage();
        }
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
}
