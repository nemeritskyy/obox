package ua.com.obox.dbschema.category;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ua.com.obox.dbschema.dish.DishResponse;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CategoryResponse {
    @JsonProperty("category_id")
    private String categoryId;
    @JsonProperty("name")
    private String name;
    @JsonProperty("description")
    private String description;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("menu_id")
    private String menuId;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("state")
    private String state;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("dishes")
    private List<DishResponse> dishes;
}
