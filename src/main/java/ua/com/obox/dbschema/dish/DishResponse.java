package ua.com.obox.dbschema.dish;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DishResponse {
    @JsonProperty("dish_id")
    private String dishId;
    @JsonProperty("category_id")
    private String categoryId;
    @JsonProperty("name")
    private String name;
    @JsonProperty("description")
    private String description;
    @JsonProperty("price")
    private Double price;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("weight")
    private Integer weight;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("calories")
    private Integer calories;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("image_url")
    private String imageUrl;

    @JsonProperty("state")
    private String state;
}