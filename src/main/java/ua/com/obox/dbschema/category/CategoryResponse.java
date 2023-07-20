package ua.com.obox.dbschema.category;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CategoryResponse {
    @JsonProperty("category_id")
    private String categoryId;
    @JsonProperty("name")
    private String name;
    @JsonProperty("menu_id")
    private String menuId;
}
