package ua.com.obox.dbschema.tenant;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;
import ua.com.obox.dbschema.language.Language;
import ua.com.obox.dbschema.restaurant.Restaurant;
import ua.com.obox.dbschema.tools.PreRemoveAssistant;
import ua.com.obox.dbschema.tools.State;

import javax.persistence.*;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@ToString
@Table(name = "tenant")
public class Tenant {
    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(columnDefinition = "CHAR(36)")
    @JsonIgnore
    private String tenantId;
    @JsonIgnore
    private String translationId;
    @Column(columnDefinition = "VARCHAR(8) DEFAULT '" + State.ENABLED + "'")
    @JsonIgnore
    private String state;

    private long createdAt;
    private long updatedAt;

    @OneToMany(mappedBy = "tenant", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Restaurant> restaurants;

    @OneToMany(mappedBy = "tenant", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Language> languages;

    @JsonProperty("name")
    @Transient
    private String name;

    @JsonProperty("language")
    @Transient
    private String language;

    @PreRemove
    public void beforeRemove() {
        PreRemoveAssistant.removeByEntityId(this.tenantId);
    }
}
