package ua.com.obox.dbschema.attachment;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import javax.persistence.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@ToString
@Table(name = "attachment")
public class Attachment {
    @Id
    @JsonIgnore
    private String attachmentId;
    @JsonProperty("reference_type")
    private String referenceType;
    @JsonProperty("reference_id")
    private String referenceId;
    @JsonIgnore
    private String attachmentUrl;
    @Transient
    private String attachment;
}
