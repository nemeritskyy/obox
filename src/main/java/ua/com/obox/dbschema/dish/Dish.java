package ua.com.obox.dbschema.dish;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;
import ua.com.obox.dbschema.category.Category;
import ua.com.obox.dbschema.tools.*;
import ua.com.obox.dbschema.translation.assistant.IdentifiableId;

import javax.persistence.*;

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
    @Column(columnDefinition = "CHAR(2)")
    @JsonProperty("weight_unit")
    private String weightUnit;

    @Lob
    @JsonIgnore
    private String allergens;
    @Transient
    @JsonProperty("allergens")
    @JsonDeserialize(using = StringArrayDeserializer.class)
    String[] allergensArray;
    @Lob
    @JsonIgnore
    private String marks;
    @Transient
    @JsonProperty("marks")
    @JsonDeserialize(using = StringArrayDeserializer.class)
    String[] marksArray;

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