package ua.com.obox.dbschema.tools;

import org.springframework.http.HttpStatus;
import ua.com.obox.dbschema.tools.response.BadFieldsResponse;

import java.util.HashMap;
import java.util.Map;

public class CatchResponse {
    public static void getMessage() {
        Map<String, String> responseBody = new HashMap<>();
        responseBody.put("error", "Contact the administrator to resolve the problem");
        throw new BadFieldsResponse(HttpStatus.BAD_REQUEST, responseBody);
    }
}
