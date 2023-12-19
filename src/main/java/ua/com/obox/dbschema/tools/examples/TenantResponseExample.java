package ua.com.obox.dbschema.tools.examples;

public class TenantResponseExample {
    public static final String POST_BODY = "{ \"name\": \"Kyiv Food LLC\", \"language\":\"en-US\" }";
    public static final String GET_ALL_200_RESPONSE_EXAMPLE = "[ { \"tenant_id\": \"f1af540c-3ccc-4483-997f-ff3d7d14fa36\", \"restaurant_id\": \"e33a1b35-b9e1-40dc-b67a-3618622b33c9\", \"original_language\": \"uk-UA\", \"translation_id\": \"286aa04c-c064-46e6-8886-f9d7e2bf7637\", \"content\": { \"uk-UA\": { \"name\": \"Ресторан пива\", \"address\": \"вул. Богдана Хмельницького 77\" } } } ]";
    public static final String GET_200_RESPONSE_EXAMPLE = "{ \"tenant_id\": \"f1af540c-3ccc-4483-997f-ff3d7d14fa36\", \"original_language\": \"uk-UA\", \"translation_id\": \"424c9b64-608b-40d1-a657-1f2afab09a66\", \"content\": { \"uk-UA\": { \"name\": \"Пивна історія\" } } }";
    public static final String GET_403_RESPONSE_EXAMPLE = "{ \"timestamp\": \"2023-08-24T06:34:35.850+00:00\", \"status\": 403, \"error\": \"Forbidden\", \"message\": \"Tenant with id a95de739-40fa-414a-9f62-fdaedb2a8282 forbidden\", \"path\": \"/tenants/a95de739-40fa-414a-9f62-fdaedb2a8282\" }";
    public static final String POST_201_RESPONSE_EXAMPLE = "{ \"tenant_id\": \"c6f22a4c-1d7f-4f3c-ab43-a4986db87e34\" }";
    public static final String POST_400_RESPONSE_EXAMPLE = "{ \"timestamp\": \"2023-11-01T23:37:36.189+00:00\", \"status\": 400, \"error\": \"Bad Request\", \"message\": \"400 BAD_REQUEST\", \"path\": \"/tenants/\", \"fields\": { \"name\": \"Field name is required\", \"language\": \"The language field is mandatory in the format en-US\" } }";
    public static final String PATCH_400_RESPONSE_EXAMPLE = "{ \"timestamp\": \"2023-11-01T23:46:43.429+00:00\", \"status\": 400, \"error\": \"Bad Request\", \"message\": \"400 BAD_REQUEST\", \"path\": \"/tenants/54a6486d-f394-45e5-9da0-8a99ca2eb6bf\", \"fields\": { \"name\": \"Field name is required\", \"language\": \"The language field is mandatory in the format en-US\" } }";
    public static final String ALL_MAPPINGS_404_RESPONSE_EXAMPLE = "{ \"timestamp\": \"2023-08-24T06:40:39.627+00:00\", \"status\": 404, \"error\": \"Not Found\", \"message\": \"Tenant with id b2268525-099d-4e8e-80ce-x258066c3aec not found\", \"path\": \"/tenants/b2268525-099d-4e8e-80ce-x258066c3aec\" }";

}
