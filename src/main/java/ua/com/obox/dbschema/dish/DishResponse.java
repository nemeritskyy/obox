package ua.com.obox.dbschema.dish;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ua.com.obox.dbschema.associateddata.RestaurantAssociatedData;
import ua.com.obox.dbschema.associateddata.RestaurantAssociatedDataRepository;

import java.util.TreeMap;

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
    @JsonProperty("name")
    private String name;
    @JsonInclude(JsonInclude.Include.NON_NULL)
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

    @JsonIgnore
    private String inputAllergens;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("allergens")
    private TreeMap<String, Boolean> allergens;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("image_url")
    private String imageUrl;
    @JsonProperty("state")
    private String state;

    @JsonIgnore
    private String associatedId;
    @JsonIgnore
    private RestaurantAssociatedDataRepository dataRepository;

    @JsonProperty("allergens")
    private TreeMap<String, Boolean> setAllergens() {
        TreeMap<String, Boolean> allergensResponse = new TreeMap<>();
        boolean contains = false;

        if (associatedId != null) {
            var dataInfo = dataRepository.findByAssociatedId(associatedId);
            RestaurantAssociatedData associatedData = dataInfo.orElseThrow();
            for (String allergen : associatedData.getAllergens().split("::")) {
                contains = inputAllergens.contains(allergen);
                allergensResponse.put(allergen, contains);
            }
        }
        return allergensResponse.size() == 0 ? null : allergensResponse;
    }
}
