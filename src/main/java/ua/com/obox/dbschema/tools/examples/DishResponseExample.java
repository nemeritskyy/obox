package ua.com.obox.dbschema.tools.examples;

public class DishResponseExample {
    public static final String GET_200_RESPONSE_EXAMPLE = "{ \"dish_id\": \"762dbe8e-4cfc-40f0-af3f-02cfc67da709\", \"category_id\": \"decb23a3-b437-4c28-95df-a93aa59d90a7\", \"associated_id\": \"915e9be8-a36b-40d9-bf9b-aeedc180958e\", \"name\": \"Dorblue\", \"description\": \"string\", \"price\": 300.0, \"weight\": 250, \"calories\": 400, \"allergens\": [ \"Celery\", \"Cereals containing gluten\", \"Clams\" ], \"tags\": [ \"Spicy\", \"Vegan\" ], \"state\": \"ENABLED\" }";
    public static final String POST_BODY = "{ \"category_id\": \"uuid\", \"name\": \"string\", \"description\": \"string\", \"price\": 1.99, \"weight\": 0, \"calories\": 0, \"allergens\": [ \"Celery\", \"Cereals containing gluten\", \"Clams\" ], \"tags\": [ \"Spicy\", \"Vegan\" ], \"state\": \"ENABLED or DISABLED\" }";
    public static final String POST_201_RESPONSE_EXAMPLE = "{ \"dish_id\": \"762dbe8e-4cfc-40f0-af3f-02cfc67da709\" }";
    public static final String POST_400_RESPONSE_EXAMPLE = "{ \"timestamp\": \"2023-08-23T08:06:10.306+00:00\", \"status\": 400, \"error\": \"Bad Request\", \"message\": \"400 BAD_REQUEST\", \"path\": \"/dishes/\", \"fields\": { \"category_id\": \"Category with id null not found\", \"price\": \"Price cannot be empty\", \"name\": \"Field name is required\", \"state\": \"The state is incorrect\" } }";
    public static final String PATCH_BODY = "{ \"category_id\": \"uuid\", \"name\": \"string\", \"description\": \"string\", \"price\": 1.99, \"weight\": 0, \"calories\": 0, \"allergens\": [ \"Celery\", \"Cereals containing gluten\", \"Clams\" ], \"tags\": [ \"Spicy\", \"Vegan\" ], \"state\": \"ENABLED or DISABLED\" }";
    public static final String PATCH_400_RESPONSE_EXAMPLE = "";
    public static final String ALL_MAPPINGS_404_RESPONSE_EXAMPLE = "{ \"timestamp\": \"2023-08-23T08:00:50.333+00:00\", \"status\": 404, \"error\": \"Not Found\", \"message\": \"Dish with id 762dbe8e-4cxc-40f0-af3f-02cfc67da709 not found\", \"path\": \"/dishes/762dbe8e-4cxc-40f0-af3f-02cfc67da709\" }";
}
