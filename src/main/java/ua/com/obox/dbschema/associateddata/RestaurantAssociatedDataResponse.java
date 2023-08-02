package ua.com.obox.dbschema.associateddata;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    private String allergens;
    @JsonProperty("tags")
    private String tags;
}
