package ua.com.obox.dbschema.associateddata;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.*;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@ToString
@Table(name = "RestaurantAssociatedData")
public class RestaurantAssociatedData {
    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(columnDefinition = "CHAR(36)")
    @JsonIgnore
    private String associatedId;

    private String restaurantId;
    private String languageCode;
    private String allergens;
    private String tags;

    public RestaurantAssociatedData(String languageCode, String restaurantId) {
        this.languageCode = languageCode;
        this.allergens = setDefaultAllergens(languageCode);
        this.tags = setDefaultTags(languageCode);
        this.restaurantId = restaurantId;

    }

    @JsonIgnore
    public String setDefaultAllergens(String languageCode) {
        List<String> allergens = new ArrayList<>();
        if (languageCode.equals("ua")) {
            allergens.add("Крупи, що містять глютен");
            allergens.add("Ракоподібні");
            allergens.add("Яйця");
            allergens.add("Риба");
            allergens.add("Арахіс");
            allergens.add("Соя");
            allergens.add("Молоко");
            allergens.add("Горіхи");
            allergens.add("Селера");
            allergens.add("Гірчиця");
            allergens.add("Кунжут");
            allergens.add("Двоокис сірки та сульфіти");
            allergens.add("Люпин");
            allergens.add("Молюски");
            allergens.add("Мед");
        }
        if (languageCode.equals("en")) {
            allergens.add("Cereals containing gluten");
            allergens.add("Shellfish");
            allergens.add("Eggs");
            allergens.add("Fish");
            allergens.add("Peanut");
            allergens.add("Soy");
            allergens.add("Milk");
            allergens.add("Nuts");
            allergens.add("Celery");
            allergens.add("Mustard");
            allergens.add("Sesame");
            allergens.add("Sulfur dioxide and sulfites");
            allergens.add("Lupine");
            allergens.add("Clams");
            allergens.add("Honey");
        }
        return allergens.stream().sorted()
                .collect(Collectors.joining("::"));
    }

    @JsonIgnore
    public String setDefaultTags(String languageCode) {
        List<String> tags = new ArrayList<>();
        if (languageCode.equals("ua")) {
            tags.add("Вегетаріанський");
            tags.add("Не містить глютену");
            tags.add("Гостре");
            tags.add("Середньо-гострий");
            tags.add("Рекомендовано");
            tags.add("Веганське");
        }
        if (languageCode.equals("en")) {
            tags.add("Vegetarian");
            tags.add("Does not contain gluten");
            tags.add("Spicy");
            tags.add("Medium-spicy");
            tags.add("Recommended");
            tags.add("Vegan");
        }
        return tags.stream().sorted()
                .collect(Collectors.joining("::"));
    }

    public String getRestaurantId() {
        return restaurantId;
    }

    public void setRestaurantId(String restaurantId) {
        this.restaurantId = restaurantId;
    }

    public String getAllergens() {
        return allergens;
    }
}
