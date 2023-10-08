package ua.com.obox.dbschema.tools.examples;

public class MenuResponseExample {
    public static final String GET_ALL_200_RESPONSE_EXAMPLE = "[ { \"category_id\": \"dec66073-063c-4c58-ac97-2aab5b559dd4\", \"name\": \"Fresh\", \"description\": null, \"menu_id\": \"05b303aa-8a26-4f80-9c7c-42d13ecc6348\", \"state\": \"ENABLED\" }, { \"category_id\": \"fd236b1e-8103-4c06-872c-c796262aa795\", \"name\": \"Vegetables\", \"description\": null, \"menu_id\": \"05b303aa-8a26-4f80-9c7c-42d13ecc6348\", \"state\": \"ENABLED\" }, { \"category_id\": \"a031ee28-4a62-4f76-a5e5-ca2acd81d384\", \"name\": \"To Go\", \"description\": null, \"menu_id\": \"05b303aa-8a26-4f80-9c7c-42d13ecc6348\", \"state\": \"ENABLED\" }, { \"category_id\": \"cc7bf317-c23d-4ca8-8ac6-3b204db566f3\", \"name\": \"New category\", \"description\": null, \"menu_id\": \"05b303aa-8a26-4f80-9c7c-42d13ecc6348\", \"state\": \"DISABLED\" } ]\n";
    public static final String GET_200_RESPONSE_EXAMPLE = "{ \"menu_id\": \"f08de446-7622-4c31-b5bd-a4da773ca374\", \"restaurant_id\": \"9aff3e00-451c-490e-b48b-c4315785b75e\", \"name\": \"Bar ourside\", \"language_code\": \"es\", \"state\": \"ENABLED\" }\n";
    public static final String POST_201_RESPONSE_EXAMPLE = "{ \"menu_id\": \"b4d331ec-ec54-4789-b8f3-1b3435eaa52a\" }";
    public static final String POST_400_RESPONSE_EXAMPLE = "{ \"timestamp\": \"2023-10-05T13:46:15.440+00:00\", \"status\": 400, \"error\": \"Bad Request\", \"message\": \"400 BAD_REQUEST\", \"path\": \"/menus/\", \"fields\": { \"language_code\": \"The language code must contain from 2 to 3 characters\", \"restaurant_id\": \"Restaurant with id 9affe00-451c-490e-b48b-c4315785b75e not found\", \"name\": \"Field name is required\", \"state\": \"The state is incorrect\" } }";
    public static final String PATCH_400_RESPONSE_EXAMPLE = "{ \"timestamp\": \"2023-10-05T13:36:35.511+00:00\", \"status\": 400, \"error\": \"Bad Request\", \"message\": \"400 BAD_REQUEST\", \"path\": \"/menus/f08de446-7622-4c31-b5bd-a4da773ca374\", \"fields\": { \"name\": \"Field name is required\", \"state\": \"The state is incorrect\" } }";
    public static final String ALL_MAPPINGS_404_RESPONSE_EXAMPLE = "{ \"timestamp\": \"2023-08-25T05:10:43.103+00:00\", \"status\": 404, \"error\": \"Not Found\", \"message\": \"Menu with id 05b303aa-8a26-4fx0-9c7c-42d13ecc6348 not found\", \"path\": \"/menus/05b303aa-8a26-4fx0-9c7c-42d13ecc6348\" }";
}