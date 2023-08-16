package ua.com.obox.dbschema.tools.services;

public interface CommonResponseService {
    void notFoundResponse(String entityId);
    void forbiddenResponse(String entityId);
    void badRequestResponse(String entityId);
}
