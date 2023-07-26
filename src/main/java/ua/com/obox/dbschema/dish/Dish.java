package ua.com.obox.dbschema.dish;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;
import ua.com.obox.dbschema.category.Category;
import ua.com.obox.dbschema.tools.State;

import javax.persistence.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@ToString
@Table(name = "dish")
public class Dish {
    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(columnDefinition = "CHAR(36)")
    @JsonIgnore
    private String dishId;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    @JsonIgnore
    private Category category;
    private String name;
    private Double price;
    private String description;
    private Integer calories;
    private Integer weight;
    @JsonIgnore
    private String imageUrl;
    @Column(columnDefinition = "VARCHAR(7) DEFAULT '" + State.ENABLE + "'")
    private String state;
    @Transient
    private String category_id;
    @Transient
    @Schema(description = "Dish picture")
    private String image;

    @JsonIgnore
    public void setCategoryIdForDish(String category_id) {
        Category category = new Category();
        category.setCategoryId(category_id);
        this.category = category;
    }
}