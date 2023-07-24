package ua.com.obox.dbschema.tools;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import ua.com.obox.dbschema.tools.exception.Message;
import ua.com.obox.dbschema.tools.logging.LogLevel;
import ua.com.obox.dbschema.tools.logging.LoggingService;

import java.util.UUID;


@RequiredArgsConstructor
public class Validator {
    public static void validateName(String loggingMessage, String name, LoggingService loggingService) {
        name = name.trim(); // delete whitespaces
        if (name.isEmpty()) {
            loggingService.log(LogLevel.ERROR, loggingMessage + Message.ERROR.getMessage() + Message.REQUIRED.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, Message.REQUIRED.getMessage().trim());
        }
        if (name.length() > 200) {
            loggingService.log(LogLevel.ERROR, loggingMessage + Message.ERROR.getMessage() + Message.LIMIT_200.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, Message.LIMIT_200.getMessage().trim());
        }
    }

    public static void validateVarchar(String loggingMessage, String fieldName, String str, LoggingService loggingService) {
        str = str.trim(); // delete whitespaces
        if (str.length() > 255) {
            loggingService.log(LogLevel.ERROR, loggingMessage + Message.ERROR.getMessage() + Message.LIMIT_255.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, fieldName + Message.LIMIT_255.getMessage());
        }
    }

    public static void checkUUID(String loggingMessage, String uuid, LoggingService loggingService) {
        try {
            UUID.fromString(uuid);
        } catch (IllegalArgumentException e) {
            loggingService.log(LogLevel.ERROR, loggingMessage + Message.BAD_UUID.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, loggingMessage + " uuid=" + uuid + Message.BAD_UUID.getMessage());
        }
    }

    public static void checkPrice(String loggingMessage, Double price, LoggingService loggingService) {
        if (price < 0 || price > 100000) {
            loggingService.log(LogLevel.ERROR, loggingMessage + Message.ERROR.getMessage() + Message.CHECK_PRICE.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, Message.CHECK_PRICE.getMessage().trim());
        }
    }

    public static String detectImageType(byte[] imageData){
        if (imageData.length >= 8) {
            byte[] jpegSignature = { (byte) 0xFF, (byte) 0xD8 };
            byte[] pngSignature = { (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A };

            if (startsWith(imageData, jpegSignature)) {
                return ".jpg";
            } else if (startsWith(imageData, pngSignature)) {
                return ".png";
            }
        }
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
