package ua.com.obox.dbschema.menu;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;
import ua.com.obox.dbschema.associateddata.RestaurantAssociatedData;
import ua.com.obox.dbschema.associateddata.RestaurantAssociatedDataRepository;
import ua.com.obox.dbschema.category.Category;
import ua.com.obox.dbschema.restaurant.Restaurant;
import ua.com.obox.dbschema.sorting.EntityOrder;
import ua.com.obox.dbschema.sorting.EntityOrderRepository;
import ua.com.obox.dbschema.tools.State;
import ua.com.obox.dbschema.tools.attachment.ApplicationContextProvider;

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
    @Column(columnDefinition = "VARCHAR(8) DEFAULT '" + State.ENABLED + "'")
    private String state;
    @OneToMany(mappedBy = "menu", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Category> categories;
    private String language_code;

    private long createdAt;
    private long updatedAt;

    @Transient
    private String restaurant_id;

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

    @PreRemove
    public void beforeRemove() {
        EntityOrderRepository entityOrderRepository = ApplicationContextProvider.getBean(EntityOrderRepository.class);
        entityOrderRepository.findByEntityId(this.menuId).ifPresent(entityOrderRepository::delete);
        EntityOrder existSorted = entityOrderRepository.findBySortedListContaining(this.menuId).orElseGet(() -> null);
        if (existSorted != null) {
            String[] elements = existSorted.getSortedList().split(",");
            StringBuilder result = new StringBuilder();
            for (String element : elements) {
                if (!element.equals(this.menuId)) {
                    result.append(element).append(",");
                }
            }
            if (result.length() > 0) {
                result.setLength(result.length() - 1);
                existSorted.setSortedList(result.toString());
                entityOrderRepository.save(existSorted);
            } else {
                entityOrderRepository.delete(existSorted);
            }
        }
    }
}
