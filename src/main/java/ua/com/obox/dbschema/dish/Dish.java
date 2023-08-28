package ua.com.obox.dbschema.dish;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;
import ua.com.obox.dbschema.category.Category;
import ua.com.obox.dbschema.tools.State;
import ua.com.obox.dbschema.tools.EmptyIntegerDeserializer;
import ua.com.obox.dbschema.tools.logging.EmptyStringDeserializer;

import javax.persistence.*;
import java.util.List;

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
    @JsonIgnore
    private String allergens;
    @Transient
    @JsonProperty("allergens")
    private List<String> listAllergens;
    @JsonIgnore
    private String tags;
    @Transient
    @JsonProperty("tags")
    private List<String> listTags;
    @Column(columnDefinition = "VARCHAR(8) DEFAULT '" + State.ENABLED + "'")
    private String state;
    private String associatedId;
    @Transient
    private String category_id;
    @Transient
    @Schema(description = "Dish picture")
    @JsonDeserialize(using = EmptyStringDeserializer.class)
    private String image;

    @JsonIgnore
    public void setCategoryIdForDish(String category_id) {
        Category category = new Category();
        category.setCategoryId(category_id);
        this.category = category;
    }
}
