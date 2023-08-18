package ua.com.obox.dbschema.menu;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;
import ua.com.obox.dbschema.associateddata.RestaurantAssociatedData;
import ua.com.obox.dbschema.associateddata.RestaurantAssociatedDataRepository;
import ua.com.obox.dbschema.category.Category;
import ua.com.obox.dbschema.restaurant.Restaurant;

import javax.persistence.*;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@ToString
@Table(name = "menu")
public class Menu {
    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(columnDefinition = "CHAR(36)")
    @JsonIgnore
    private String menuId;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id")
    @JsonIgnore
    private Restaurant restaurant;
    private String name;
    @Transient
    private String restaurant_id;
    @OneToMany(mappedBy = "menu", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Category> categories;
    private String language_code;

    @JsonIgnore
    public void checkAssociatedData(String restaurant_id, String languageCode, RestaurantAssociatedDataRepository dataRepository) {
        Restaurant restaurant = new Restaurant();
        restaurant.setRestaurantId(restaurant_id);
        if (languageCode != null && dataRepository.findByRestaurantIdAndLanguageCode(restaurant_id, languageCode.toLowerCase()).isEmpty()) {
            RestaurantAssociatedData associatedData = new RestaurantAssociatedData(languageCode.toLowerCase(), restaurant_id);
            dataRepository.save(associatedData);
        }
        language_code = languageCode.toLowerCase();
        this.restaurant = restaurant;
    }
}
