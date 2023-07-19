package ua.com.obox.dbschema.menu;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MenuResponse {
    @JsonProperty("menu_id")
    private String menuId;
    @JsonProperty("name")
    private String name;
    @JsonProperty("restaurant_id")
    private String restaurantId;
}
