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
public class RestaurantResponseId {
    @JsonProperty("restaurant_id")
    private String restaurantId;
}
