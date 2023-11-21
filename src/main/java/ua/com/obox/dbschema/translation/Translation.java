package ua.com.obox.dbschema.translation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "translation")
public class Translation {
    @Id
    @GeneratedValue(generator = "uuid2")
    @Column(name = "translation_id", columnDefinition = "CHAR(36)")
    private String translationId;

    @JoinColumn(name = "reference_id", columnDefinition = "CHAR(36)")
    private String referenceId;

    @JoinColumn(name = "reference_type")
    private String referenceType;

    @Lob
    @JoinColumn(name = "content")
    private String content;

    private long createdAt;
    private long updatedAt;
}