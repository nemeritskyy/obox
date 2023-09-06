package ua.com.obox.dbschema.tools.examples;

public class MenuResponseExample {
    public static final String GET_ALL_200_RESPONSE_EXAMPLE = "[ { \"category_id\": \"dec66073-063c-4c58-ac97-2aab5b559dd4\", \"name\": \"Fresh\", \"menu_id\": \"05b303aa-8a26-4f80-9c7c-42d13ecc6348\" }, { \"category_id\": \"fd236b1e-8103-4c06-872c-c796262aa795\", \"name\": \"Vegetables\", \"menu_id\": \"05b303aa-8a26-4f80-9c7c-42d13ecc6348\" } ]";
    public static final String GET_200_RESPONSE_EXAMPLE = "{ \"menu_id\": \"05b303aa-8a26-4f80-9c7c-42d13ecc6348\", \"restaurant_id\": \"9aff3e00-451c-490e-b48b-c4315785b75e\", \"name\": \"Non alcohol bar\", \"language_code\": \"en\" }";
    public static final String POST_201_RESPONSE_EXAMPLE = "{ \"menu_id\": \"b4d331ec-ec54-4789-b8f3-1b3435eaa52a\" }";
    public static final String POST_400_RESPONSE_EXAMPLE = "{ \"timestamp\": \"2023-08-25T05:11:14.711+00:00\", \"status\": 400, \"error\": \"Bad Request\", \"message\": \"400 BAD_REQUEST\", \"path\": \"/menus/\", \"fields\": { \"language_code\": \"Bad language code must contain from 2 to 3 characters\", \"restaurant_id\": \"Restaurant with id null not found\", \"name\": \"Field name is required\" } }";
    public static final String PATCH_400_RESPONSE_EXAMPLE = "{ \"timestamp\": \"2023-08-25T05:15:40.318+00:00\", \"status\": 400, \"error\": \"Bad Request\", \"message\": \"400 BAD_REQUEST\", \"path\": \"/menus/b4d331ec-ec54-4789-b8f3-1b3435eaa52a\", \"fields\": { \"name\": \"Field name is required\" } }";
    public static final String ALL_MAPPINGS_404_RESPONSE_EXAMPLE = "{ \"timestamp\": \"2023-08-25T05:10:43.103+00:00\", \"status\": 404, \"error\": \"Not Found\", \"message\": \"Menu with id 05b303aa-8a26-4fx0-9c7c-42d13ecc6348 not found\", \"path\": \"/menus/05b303aa-8a26-4fx0-9c7c-42d13ecc6348\" }";
}
