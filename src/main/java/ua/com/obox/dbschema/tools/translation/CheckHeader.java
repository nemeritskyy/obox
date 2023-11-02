package ua.com.obox.dbschema.tools.translation;

public class CheckHeader {
    public static String checkHeaderLanguage(String acceptLanguage) {
        return acceptLanguage != null && (acceptLanguage.equals("en-US") || acceptLanguage.equals("uk-UA")) ? acceptLanguage : "en-US";
    }
}