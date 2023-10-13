package ua.com.obox.dbschema.category;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;
import ua.com.obox.dbschema.menu.Menu;
import ua.com.obox.dbschema.dish.Dish;
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
@Table(name = "category")
public class Category {
    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(columnDefinition = "CHAR(36)")
    @JsonIgnore
    private String categoryId;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "menu_id")
    @JsonIgnore
    private Menu menu;
    private String name;
    private String description;
    @Column(columnDefinition = "VARCHAR(8) DEFAULT '" + State.ENABLED + "'")
    private String state;
    @OneToMany(mappedBy = "category", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Dish> dishes;

    private long createdAt;
    private long updatedAt;

    @Transient
    private String menu_id;

    @JsonIgnore
    public void setMenuIdForCategory(String menu_id) {
        Menu menu = new Menu();
        menu.setMenuId(menu_id);
        this.menu = menu;
    }

    @PreRemove
    public void beforeRemove() {
        EntityOrderRepository entityOrderRepository = ApplicationContextProvider.getBean(EntityOrderRepository.class);
        entityOrderRepository.findByEntityId(this.categoryId).ifPresent(entityOrderRepository::delete);
        EntityOrder existSorted = entityOrderRepository.findBySortedListContaining(this.categoryId).orElseGet(() -> null);
        if (existSorted != null) {
            String[] elements = existSorted.getSortedList().split(",");
            StringBuilder result = new StringBuilder();
            for (String element : elements) {
                if (!element.equals(this.categoryId)) {
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
