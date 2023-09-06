package ua.com.obox.dbschema.tools.examples;

public class TenantResponseExample {
    public static final String GET_ALL_200_RESPONSE_EXAMPLE = "[ { \"restaurant_id\": \"24eee05a-f05f-4b37-996b-b5a5d4070d28\", \"tenant_id\": \"3ed048e5-67b6-4ee6-a0e0-7ecb9d36bcaf\", \"address\": null, \"name\": \"Shaurma Ot Ali\" }, { \"restaurant_id\": \"c656199b-de65-4d5b-9a43-d98eed3e414f\", \"tenant_id\": \"3ed048e5-67b6-4ee6-a0e0-7ecb9d36bcaf\", \"address\": \"Kyiv, st. Tupoleva\", \"name\": \"Belissimo\" } ]";
    public static final String GET_200_RESPONSE_EXAMPLE = "{ \"tenant_id\": \"e8978620-fcd2-458a-9665-3bb4b4b5b66d\", \"name\": \"Syshi Hushiya\" }";
    public static final String GET_403_RESPONSE_EXAMPLE = "{ \"timestamp\": \"2023-08-24T06:34:35.850+00:00\", \"status\": 403, \"error\": \"Forbidden\", \"message\": \"Tenant with id a95de739-40fa-414a-9f62-fdaedb2a8282 forbidden\", \"path\": \"/tenants/a95de739-40fa-414a-9f62-fdaedb2a8282\" }";
    public static final String POST_201_RESPONSE_EXAMPLE = "{ \"tenant_id\": \"c6f22a4c-1d7f-4f3c-ab43-a4986db87e34\" }";
    public static final String POST_400_RESPONSE_EXAMPLE = "{ \"timestamp\": \"2023-08-24T05:53:13.926+00:00\", \"status\": 400, \"error\": \"Bad Request\", \"message\": \"400 BAD_REQUEST\", \"path\": \"/tenants/\", \"fields\": { \"name\": \"Field name is required\" } }";
    public static final String PATCH_400_RESPONSE_EXAMPLE = "{ \"timestamp\": \"2023-08-24T06:59:51.321+00:00\", \"status\": 400, \"error\": \"Bad Request\", \"message\": \"400 BAD_REQUEST\", \"path\": \"/tenants/818059d7-bd65-44f5-b2ab-c4d716541a43\", \"fields\": { \"name\": \"Field name is required\" } }";
    public static final String ALL_MAPPINGS_404_RESPONSE_EXAMPLE = "{ \"timestamp\": \"2023-08-24T06:40:39.627+00:00\", \"status\": 404, \"error\": \"Not Found\", \"message\": \"Tenant with id b2268525-099d-4e8e-80ce-x258066c3aec not found\", \"path\": \"/tenants/b2268525-099d-4e8e-80ce-x258066c3aec\" }";

}
