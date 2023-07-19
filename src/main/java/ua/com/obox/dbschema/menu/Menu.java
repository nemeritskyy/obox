package ua.com.obox.dbschema.menu;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;
import ua.com.obox.dbschema.restaurant.Restaurant;

import javax.persistence.*;

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

    @JsonIgnore
    public void setRestaurantIdForMenu(String restaurant_id) {
        Restaurant restaurant = new Restaurant();
        restaurant.setRestaurantId(restaurant_id);
        this.restaurant = restaurant;
    }
}
