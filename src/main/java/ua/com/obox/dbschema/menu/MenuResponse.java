package ua.com.obox.dbschema.menu;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ua.com.obox.dbschema.category.CategoryResponse;
import ua.com.obox.dbschema.translation.responsebody.Content;
import ua.com.obox.dbschema.translation.responsebody.MenuTranslationEntry;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonPropertyOrder({"restaurant_id", "menu_id", "original_language", "translation_id", "state", "content", "categories"})
public class MenuResponse {
    @JsonProperty("restaurant_id")
    private String restaurantId;
    @JsonProperty("menu_id")
    private String menuId;
    @JsonProperty("original_language")
    private String originalLanguage;
    @JsonProperty("translation_id")
    private String translationId;
    @JsonProperty("state")
    private String state;
    @JsonUnwrapped
    private Content<MenuTranslationEntry> content;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("categories")
    private List<CategoryResponse> categories;
}
