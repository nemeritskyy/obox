package ua.com.obox.dbschema.menu;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;
import ua.com.obox.dbschema.category.Category;
import ua.com.obox.dbschema.restaurant.Restaurant;
import ua.com.obox.dbschema.tools.PreRemoveAssistant;
import ua.com.obox.dbschema.tools.State;
import ua.com.obox.dbschema.translation.assistant.IdentifiableId;

import javax.persistence.*;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@ToString
@Table(name = "menu")
public class Menu implements IdentifiableId {
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
    @Column(columnDefinition = "VARCHAR(8) DEFAULT '" + State.ENABLED + "'")
    private String state;

    @JsonIgnore
    private String translationId;

    @JsonIgnore
    private long createdAt;
    @JsonIgnore
    private long updatedAt;
    @JsonIgnore
    @Column(columnDefinition = "CHAR(5)")
    private String originalLanguage;

    @OneToMany(mappedBy = "menu", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Category> categories;

    @JsonProperty("restaurant_id")
    @Transient
    private String restaurantId;

    @JsonProperty("name")
    @Transient
    private String name;

    @JsonProperty("language")
    @Transient
    private String language;

    @PreRemove
    public void beforeRemove() {
        PreRemoveAssistant.removeByEntityId(this.menuId);
    }

    @Override
    @Transient
    public String getId() {
        return this.menuId;
    }

    @Override
    @Transient
    public String getName() {
        return this.name;
    }
}