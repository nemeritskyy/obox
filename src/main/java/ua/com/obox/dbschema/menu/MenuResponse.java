package ua.com.obox.dbschema.menu;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ua.com.obox.dbschema.category.CategoryResponse;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MenuResponse {
    @JsonProperty("menu_id")
    private String menuId;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("restaurant_id")
    private String restaurantId;
    @JsonProperty("name")
    private String name;
    @JsonProperty("language_code")
    private String language;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("categories")
    private List<CategoryResponse> categories;
}
