package ua.com.obox.dbschema.restaurant;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ua.com.obox.dbschema.translation.responsebody.Content;
import ua.com.obox.dbschema.translation.responsebody.RestaurantTranslationEntry;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonPropertyOrder({"tenant_id", "restaurant_id", "translation_id", "content"})
public class RestaurantResponse {
    @JsonProperty("restaurant_id")
    private String restaurantId;
    @JsonProperty("tenant_id")
    private String tenantId;
    @JsonProperty("translation_id")
    private String translationId;
    @JsonUnwrapped
    private Content<RestaurantTranslationEntry> content;
}
