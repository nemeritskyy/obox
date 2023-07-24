package ua.com.obox.dbschema.menuitem;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;
import ua.com.obox.dbschema.category.Category;

import javax.persistence.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@ToString
@Table(name = "menu_item")
public class MenuItem {
    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(columnDefinition = "CHAR(36)")
    @JsonIgnore
    private String itemId;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    @JsonIgnore
    private Category category;
    private String name;
    private Double price;
    private String description;
    @JsonIgnore
    private Boolean visibility;
    @Transient
    private String category_id;
    @Transient
    private String image;

    @JsonIgnore
    public void setCategoryIdForMenuItem(String category_id) {
        Category category = new Category();
        category.setCategoryId(category_id);
        this.category = category;
    }
}
