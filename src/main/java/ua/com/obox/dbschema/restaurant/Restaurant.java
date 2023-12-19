package ua.com.obox.dbschema.restaurant;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;
import ua.com.obox.dbschema.menu.Menu;
import ua.com.obox.dbschema.tenant.Tenant;
import ua.com.obox.dbschema.tools.PreRemoveAssistant;

import javax.persistence.*;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@ToString
@Table(name = "restaurant")
public class Restaurant {
    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(columnDefinition = "CHAR(36)")
    @JsonIgnore
    private String restaurantId;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id")
    private Tenant tenant;

    @JsonIgnore
    private String translationId;
    @JsonIgnore
    private long createdAt;
    @JsonIgnore
    private long updatedAt;
    @JsonIgnore
    @Column(columnDefinition = "CHAR(5)")
    private String originalLanguage;

    @OneToMany(mappedBy = "restaurant", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Menu> menus;

    @JsonProperty("tenant_id")
    @Transient
    private String tenantId;

    @JsonProperty("name")
    @Transient
    private String name;

    @JsonProperty("address")
    @Transient
    private String address;

    @JsonProperty("language")
    @Transient
    private String language;

    @JsonIgnore
    public void setTenantIdForRestaurant(String tenantId) {
        Tenant tenant = new Tenant();
        tenant.setTenantId(tenantId);
        this.tenant = tenant;
    }

    @PreRemove
    public void beforeRemove() {
        PreRemoveAssistant.removeByEntityId(this.restaurantId, "restaurant");
    }
}
