package ua.com.obox.dbschema.dish;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;
import ua.com.obox.dbschema.category.Category;
import ua.com.obox.dbschema.tools.State;
import ua.com.obox.dbschema.tools.EmptyIntegerDeserializer;

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
    @JsonDeserialize(using = EmptyIntegerDeserializer.class)
    private Integer calories;
    @JsonDeserialize(using = EmptyIntegerDeserializer.class)
    private Integer weight;
    @JsonIgnore
    private String imageUrl;
    private String allergens;
    private String tags;
    @Column(columnDefinition = "VARCHAR(8) DEFAULT '" + State.ENABLED + "'")
    private String state;
    private String associatedId;
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
