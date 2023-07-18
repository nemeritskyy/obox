package ua.com.obox.dbschema.tools.exception;

public class ExceptionTools {
    public static String generateLoggingMessage(String methodName, String parameter) {
        return methodName + " with parameter " + parameter;
    }
}
