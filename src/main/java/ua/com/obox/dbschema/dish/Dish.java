package ua.com.obox.dbschema.dish;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;
import ua.com.obox.dbschema.category.Category;
import ua.com.obox.dbschema.tools.EmptyDoubleDeserializer;
import ua.com.obox.dbschema.tools.PreRemoveAssistant;
import ua.com.obox.dbschema.tools.State;
import ua.com.obox.dbschema.tools.EmptyIntegerDeserializer;
import ua.com.obox.dbschema.translation.assistant.IdentifiableId;

import javax.persistence.*;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@ToString
@Table(name = "dish")
public class Dish implements IdentifiableId {
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

    @JsonDeserialize(using = EmptyDoubleDeserializer.class)
    private Double price;
    @JsonDeserialize(using = EmptyDoubleDeserializer.class)
    @JsonProperty("special_price")
    private Double specialPrice;

    @JsonDeserialize(using = EmptyIntegerDeserializer.class)
    @JsonProperty("cooking_time")
    private Integer cookingTime;
    @JsonDeserialize(using = EmptyIntegerDeserializer.class)
    private Integer calories;

    private String weight;
    @Column(columnDefinition = "CHAR(36)")
    @JsonProperty("weight_unit")
    private String weightUnit;

    @JsonIgnore
    private String allergens;
    @JsonIgnore
    private String tags;
    @Column(columnDefinition = "VARCHAR(8) DEFAULT '" + State.ENABLED + "'")
    private String state;
    @Column(columnDefinition = "VARCHAR(8) DEFAULT '" + State.ENABLED + "'")
    @JsonProperty("in_stock")
    private String inStock;
    @JsonIgnore
    private String translationId;

    private String image;

    private long createdAt;
    private long updatedAt;

    @Transient
    @JsonProperty("allergens")
    private List<String> listAllergens;

    @Transient
    @JsonProperty("tags")
    private List<String> listTags;

    @JsonProperty("category_id")
    @Transient
    private String categoryId;

    @JsonProperty("name")
    @Transient
    private String name;

    @JsonProperty("description")
    @Transient
    private String description;

    @JsonProperty("language")
    @Transient
    private String language;

    @PreRemove
    public void beforeRemove() {
        PreRemoveAssistant.removeAttachmentByEntityId(this.dishId);
        PreRemoveAssistant.removeByEntityId(this.dishId);
    }

    @Override
    @Transient
    public String getId() {
        return this.dishId;
    }

    @Override
    @Transient
    public String getName() {
        return this.name;
    }
}