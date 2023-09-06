package ua.com.obox.dbschema.tools.examples;

public class RestaurantResponseExample {
    public static final String GET_ALL_200_RESPONSE_EXAMPLE = "[ { \"menu_id\": \"05b303aa-8a26-4f80-9c7c-42d13ecc6348\", \"restaurant_id\": \"9aff3e00-451c-490e-b48b-c4315785b75e\", \"name\": \"Non alcohol bar\", \"language_code\": \"en\" }, { \"menu_id\": \"0c19ee0e-ed72-4d74-a31e-56a366be9b2b\", \"restaurant_id\": \"9aff3e00-451c-490e-b48b-c4315785b75e\", \"name\": \"Hot\", \"language_code\": \"en\" } ]";
    public static final String GET_200_RESPONSE_EXAMPLE = "{ \"restaurant_id\": \"9aff3e00-451c-490e-b48b-c4315785b75e\", \"tenant_id\": \"758a4537-201f-4fdd-a4e0-eefb75c35c60\", \"address\": \"Bohdana st. 32\", \"name\": \"Summer dream\" }";
    public static final String POST_201_RESPONSE_EXAMPLE = "{ \"menu_id\": \"0c19ee0e-ed72-4d74-a31e-56a366be9b2b\" }";
    public static final String POST_400_RESPONSE_EXAMPLE = "{ \"timestamp\": \"2023-08-24T11:58:43.121+00:00\", \"status\": 400, \"error\": \"Bad Request\", \"message\": \"400 BAD_REQUEST\", \"path\": \"/menus/\", \"fields\": { \"tenant_id\": \"Tenant with id null not found\", \"name\": \"Field name is required\" } }";
    public static final String PATCH_400_RESPONSE_EXAMPLE = "{ \"timestamp\": \"2023-08-24T11:57:04.949+00:00\", \"status\": 400, \"error\": \"Bad Request\", \"message\": \"400 BAD_REQUEST\", \"path\": \"/restaurants/9aff3e00-451c-490e-b48b-c4315785b75e\", \"fields\": { \"name\": \"Field name is required\" } }";
    public static final String ALL_MAPPINGS_404_RESPONSE_EXAMPLE = "{ \"timestamp\": \"2023-08-24T11:53:36.794+00:00\", \"status\": 404, \"error\": \"Not Found\", \"message\": \"Restaurant with id 9aff3e00-451c-49xe-b48b-c4315785b75e not found\", \"path\": \"/restaurants/9aff3e00-451c-49xe-b48b-c4315785b75e\" }";
}
