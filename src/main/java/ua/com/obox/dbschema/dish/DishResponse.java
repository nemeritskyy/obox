package ua.com.obox.dbschema.dish;

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
    @JsonProperty("weight")
    private Integer weight;
    @JsonProperty("calories")
    private Integer calories;
    @JsonProperty("allergens")
    private List<String> allergens;
    @JsonProperty("tags")
    private List<String> tags;
    @JsonProperty("state")
    private String state;
}
