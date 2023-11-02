package ua.com.obox.dbschema.associateddata;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RestaurantAssociatedDataResponse {
    @JsonProperty("associated_id")
    private String associatedId;
    @JsonProperty("restaurant_id")
    private String restaurantId;
    @JsonProperty("language_code")
    private String languageCode;
    @JsonProperty("allergens")
    private List<String> allergens;
    @JsonProperty("tags")
    private List<String> tags;
}
