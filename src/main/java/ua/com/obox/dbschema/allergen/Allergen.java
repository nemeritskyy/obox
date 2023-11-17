package ua.com.obox.dbschema.allergen;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;
import ua.com.obox.dbschema.tools.PreRemoveAssistant;
import ua.com.obox.dbschema.translation.assistant.IdentifiableId;

import javax.persistence.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "allergen")
public class Allergen implements IdentifiableId {
    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "allergen_id", columnDefinition = "CHAR(36)")
    private String allergenId;

    @JsonProperty("reference_id")
    private String referenceId;

    @JsonProperty("reference_type")
    private String referenceType;

    @JsonProperty("name")
    @Transient
    private String name;

    @JsonIgnore
    @Column(name = "translation_id", columnDefinition = "CHAR(36)")
    private String translationId;

    private long createdAt;
    private long updatedAt;

    @JsonProperty("language")
    @Transient
    private String language;

    @PreRemove
    public void beforeRemove() {
        PreRemoveAssistant.removeByEntityId(this.allergenId);
        PreRemoveAssistant.removeAllergenFromDish(this.allergenId);
    }

    @Override
    @Transient
    public String getId() {
        return this.allergenId;
    }

    @Override
    @Transient
    public String getName() {
        return this.name;
    }
}
