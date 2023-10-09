package ua.com.obox.dbschema.tools.configuration;

public class ValidationConfiguration {
    // Dish
    public final static int MAX_PRICE = 100_000;
    public final static int MAX_CALORIES = 30_000;
    public final static int MAX_COOKING_TIME = 600; // in minutes
    public final static String WEIGHT_REGEX = "^(?:[1-9]\\d*(?:\\/[1-9]\\d*)*| [1-9]\\d*)$"; // 150/30/20

    // Attachment
    public final static int MAX_FILE_SIZE = 10 * 1024 * 1024; // in bytes
    public final static int ATTACHMENT_COMPRESSING_SIZE = 2 * 1024 * 1024; // in bytes
    public final static int ATTACHMENT_RECOMMENDED_WIDTH = 1024; // in pixels
}