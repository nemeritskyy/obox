package ua.com.obox.dbschema.language;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ua.com.obox.dbschema.tenant.Tenant;

import javax.persistence.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "language")
public class Language {
    @Id
    @GeneratedValue(generator = "uuid2")
    @Column(name = "language_id")
    private String languageId;

    @ManyToOne
    @JoinColumn(name = "tenant_id")
    @JsonIgnore
    private Tenant tenant;

    @JoinColumn(name = "name")
    private String name;

    @JoinColumn(name = "label")
    private String label;

    private long createdAt;
    private long updatedAt;
}
