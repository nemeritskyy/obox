package ua.com.obox.dbschema.tools.configuration;

import java.util.List;

public class ValidationConfiguration {
    // Dish
    public final static int MAX_PRICE = 100_000;
    public final static int MAX_CALORIES = 30_000;
    public final static int MAX_COOKING_TIME = 600; // in minutes
    public final static String WEIGHT_REGEX = "^(?:[1-9]\\d*(?:\\/[1-9]\\d*)*| [1-9]\\d*)$"; // 150/30/20
    public final static int MAX_WEIGHT = 10000;
    // Language
    public final static String LANGUAGE_REGEX = "[a-z]{2}-[A-Z]{2}";
    public final static String UUID_REGEX = "([0-9a-fA-F-]{36}(,)?)+";

    // Attachment
    public final static int MAX_FILE_SIZE = 3 * 1024 * 1024; // in bytes
    public final static int ATTACHMENT_RECOMMENDED_WIDTH = 1024; // in pixels
    public final static List<String> SUPPORT_LANGUAGES = List.of("uk-UA", "en-US");
}