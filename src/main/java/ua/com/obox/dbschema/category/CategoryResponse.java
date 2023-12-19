package ua.com.obox.dbschema.category;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ua.com.obox.dbschema.dish.DishResponse;
import ua.com.obox.dbschema.translation.responsebody.CategoryTranslationEntry;
import ua.com.obox.dbschema.translation.responsebody.Content;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonPropertyOrder({"menu_id", "category_id", "original_language", "translation_id", "state", "content", "dishes"})
public class CategoryResponse {
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("menu_id")
    private String menuId;
    @JsonProperty("category_id")
    private String categoryId;
    @JsonProperty("original_language")
    private String originalLanguage;
    @JsonProperty("translation_id")
    private String translationId;
    @JsonProperty("state")
    private String state;
    @JsonUnwrapped
    private Content<CategoryTranslationEntry> content;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("dishes")
    private List<DishResponse> dishes;
}
