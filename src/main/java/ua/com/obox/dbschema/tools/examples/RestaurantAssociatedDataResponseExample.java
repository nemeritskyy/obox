package ua.com.obox.dbschema.tools.examples;

public class RestaurantAssociatedDataResponseExample {
    public static final String GET_200_RESPONSE_EXAMPLE = "{ \"associated_id\": \"b7830129-53f1-4e1b-8a55-398462697ac0\", \"restaurant_id\": \"9aff3e00-451c-490e-b48b-c4315785b75e\", \"language_code\": \"en\", \"allergens\": [ \"Celery\", \"Cereals containing gluten\", \"Crustaceans\", \"Eggs\", \"Fish\", \"Honey\", \"Lupin\", \"Milk\", \"Molluscs\", \"Mustard\", \"Nuts\", \"Peanuts\", \"Sesame seeds\", \"Soybeans\", \"Sulphur dioxide and sulphites\" ], \"tags\": [ \"Gluten-free\", \"Medium-spicy\", \"Recommended\", \"Spicy\", \"Vegan\", \"Vegetarian\" ] }";
    public static final String ALL_MAPPINGS_404_RESPONSE_EXAMPLE ="{ \"timestamp\": \"2023-08-29T02:52:11.105+00:00\", \"status\": 404, \"error\": \"Not Found\", \"message\": \"Associated data with id b7830129-53f1-4e1b-xa55-398462697ac0 not found\", \"path\": \"/associated/b7830129-53f1-4e1b-xa55-398462697ac0\" }";
}
