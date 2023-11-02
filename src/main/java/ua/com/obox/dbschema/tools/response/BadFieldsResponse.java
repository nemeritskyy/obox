package ua.com.obox.dbschema.tools.response;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

public class BadFieldsResponse extends ResponseStatusException {
    private final Map<String, String> fields;

    public BadFieldsResponse(HttpStatus status, Map<String, String> fields) {
        super(status);
        this.fields = fields;
    }

    public Map<String, String> getFields() {
        return fields;
    }
}