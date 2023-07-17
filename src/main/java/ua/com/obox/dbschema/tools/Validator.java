package ua.com.obox.dbschema.tools;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class Validator {
    public static void validateName(String name) {
        if (name == null || name.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Name field is required");
        }
        if (name.length() > 200) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Name field must contain from 1 to 200 characters");
        }
    }
}
