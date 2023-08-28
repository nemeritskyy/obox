package ua.com.obox.dbschema.restaurant;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RestaurantResponse {
    @JsonProperty("restaurant_id")
    private String restaurantId;
    @JsonProperty("tenant_id")
    private String tenantId;
    @JsonProperty("address")
    private String address;
    @JsonProperty("name")
    private String name;
}
