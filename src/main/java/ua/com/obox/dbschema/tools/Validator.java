package ua.com.obox.dbschema.tools;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import ua.com.obox.dbschema.tools.exception.Message;
import ua.com.obox.dbschema.tools.logging.LogLevel;
import ua.com.obox.dbschema.tools.logging.LoggingService;

import java.util.Base64;


@RequiredArgsConstructor
public class Validator {
    public static String validateName(String loggingMessage, String name, LoggingService loggingService) {
        if (name == null || name.trim().isEmpty()) {
            loggingService.log(LogLevel.ERROR, loggingMessage + Message.ERROR.getMessage() + Message.REQUIRED.getMessage());
            return Message.REQUIRED.getMessage();
//            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, Message.REQUIRED.getMessage().trim());
        }
        name = name.trim(); // delete whitespaces
        if (name.length() > 200) {
            loggingService.log(LogLevel.ERROR, loggingMessage + Message.ERROR.getMessage() + Message.LIMIT_200.getMessage());
//            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, Message.LIMIT_200.getMessage().trim());
            return Message.LIMIT_200.getMessage();
        }
        return null;
    }

    public static String validateVarchar(String loggingMessage, String fieldName, String str, LoggingService loggingService) {
        if (str != null && str.trim().length() == 0 || str != null && str.length() > 255) {
            loggingService.log(LogLevel.ERROR, loggingMessage + Message.ERROR.getMessage() + Message.LIMIT_255.getMessage());
//            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, fieldName + Message.LIMIT_255.getMessage());
            return fieldName + Message.LIMIT_255.getMessage();
        }
        return null;
    }

    public static void languageCode(String loggingMessage, String code, LoggingService loggingService) {
        if (code == null || code.length() < 2 || code.length() > 3) {
            loggingService.log(LogLevel.ERROR, loggingMessage + Message.LANGUAGE.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, Message.LANGUAGE.getMessage().trim());
        }
    }

    public static void positiveInteger(String loggingMessage, Number inputInteger, int maxInteger, LoggingService loggingService) {
        if (inputInteger != null && inputInteger.doubleValue() < 0 || inputInteger != null && inputInteger.doubleValue() > maxInteger) {
            loggingService.log(LogLevel.ERROR, loggingMessage + Message.LIMIT.getMessage() + maxInteger);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, loggingMessage + Message.LIMIT.getMessage() + maxInteger);
        }
    }

    public static String validateState(String loggingMessage, String state, LoggingService loggingService) {
        if (state == null || (!state.equals(State.ENABLED) && !state.equals(State.DISABLED))) {
            loggingService.log(LogLevel.ERROR, loggingMessage + Message.BAD_STATE.getMessage());
//            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, Message.BAD_STATE.getMessage());
            return Message.BAD_STATE.getMessage();
        }
        return null;
    }

    public static boolean validateImage(String image, LoggingService loggingService) {
        byte[] imageData = new byte[0];
        try {
            imageData = Base64.getDecoder().decode(image);
        } catch (Exception exception) {
            System.out.println("Incorrect file");
        }
        return detectImageType(imageData, loggingService) != null && image.length() > 30000;
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
        loggingService.log(LogLevel.ERROR, "Bad type of upload image support only JPG and PNG");
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
