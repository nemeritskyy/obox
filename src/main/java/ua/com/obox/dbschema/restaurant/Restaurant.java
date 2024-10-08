package ua.com.obox.dbschema.restaurant;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;
import ua.com.obox.dbschema.menu.Menu;
import ua.com.obox.dbschema.sorting.EntityOrderRepository;
import ua.com.obox.dbschema.tenant.Tenant;
import ua.com.obox.dbschema.tools.attachment.ApplicationContextProvider;

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
    @JsonIgnore
    private Tenant tenant;
    private String name;
    private String address;
    @Transient
    private String tenant_id;
    @OneToMany(mappedBy = "restaurant", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Menu> menus;

    private long createdAt;
    private long updatedAt;

    @JsonIgnore
    public void setTenantIdForRestaurant(String tenant_id) {
        Tenant tenant = new Tenant();
        tenant.setTenantId(tenant_id);
        this.tenant = tenant;
    }

    @PreRemove
    public void beforeRemove() {
        EntityOrderRepository entityOrderRepository = ApplicationContextProvider.getBean(EntityOrderRepository.class);
        entityOrderRepository.findByEntityId(this.restaurantId).ifPresent(entityOrderRepository::delete);
    }
}
