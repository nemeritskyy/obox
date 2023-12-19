package ua.com.obox.dbschema.dish;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ua.com.obox.dbschema.translation.responsebody.CategoryTranslationEntry;
import ua.com.obox.dbschema.translation.responsebody.Content;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonPropertyOrder({"category_id", "dish_id", "original_language", "translation_id", "state", "in_stock", "content", "price", "special_price", "weight", "weight_unit", "cooking_time", "calories", "image", "allergens", "tags"})
public class DishResponse {
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("category_id")
    private String categoryId;
    @JsonProperty("dish_id")
    private String dishId;
    @JsonProperty("original_language")
    private String originalLanguage;
    @JsonProperty("translation_id")
    private String translationId;
    @JsonUnwrapped
    private Content<CategoryTranslationEntry> content;
    @JsonProperty("price")
    private Double price;
    @JsonProperty("special_price")
    private Double specialPrice;
    @JsonProperty("weight")
    private String weight;
    @JsonProperty("weight_unit")
    private String weightUnit;
    @JsonProperty("cooking_time")
    private Integer cookingTime;
    @JsonProperty("calories")
    private Integer calories;
    @JsonProperty("allergens")
    private List<String> allergens;
    @JsonProperty("marks")
    private List<String> marks;
    @JsonProperty("image")
    private String image;
    @JsonProperty("in_stock")
    private String inStock;
    @JsonProperty("state")
    private String state;
}
