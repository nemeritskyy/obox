package ua.com.obox.dbschema.tools.examples;

public class OrderResponseExample {
    public static final String POST_400_RESPONSE_EXAMPLE = "{ \"timestamp\": \"2023-10-15T08:07:07.201+00:00\", \"status\": 400, \"error\": \"Bad Request\", \"message\": \"400 BAD_REQUEST\", \"path\": \"/order/\", \"fields\": { \"sorted_list\": \"The UUIDs are in the wrong format, a comma-separated list is acceptable\", \"reference_type\": \"Bad reference type\" } }";
    public static final String POST_404_RESPONSE_EXAMPLE = "{ \"timestamp\": \"2023-10-15T08:11:46.931+00:00\", \"status\": 404, \"error\": \"Not Found\", \"message\": \"When trying to refresh the sorted list, the entity was not found\", \"path\": \"/order/\" }";
    public static final String POST_REQUEST_BODY_EXAMPLE = "{ \"entity_id\":\"81a51210-8c08-4f48-86d9-f15856375d6d\", \"reference_type\":\"CATEGORY\", \"sorted_list\": [ \"2cbd12a3-9f7d-458b-8509-91bf6cc5b190\", \"544f8740-9574-4be7-8633-38d29b508554\", \"1f1679bd-7c0e-4645-b70e-83473f70da81\" ] }";
}
