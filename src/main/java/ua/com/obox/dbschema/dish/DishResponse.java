package ua.com.obox.dbschema.dish;

import com.fasterxml.jackson.annotation.JsonInclude;
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
public class DishResponse {
    @JsonProperty("dish_id")
    private String dishId;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("category_id")
    private String categoryId;
    @JsonProperty("associated_id")
    private String associatedId;
    @JsonProperty("name")
    private String name;
    @JsonProperty("description")
    private String description;
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
    @JsonProperty("tags")
    private List<String> tags;
    @JsonProperty("image")
    private String image;
    @JsonProperty("in_stock")
    private String inStock;
    @JsonProperty("state")
    private String state;
}
