package ua.com.obox.dbschema.attachment;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AttachmentResponse {
    @JsonProperty("attachment_id")
    private String attachmentId;
    @JsonProperty("reference_id")
    private String referenceId;
    @JsonProperty("reference_type")
    private String referenceType;
    @JsonProperty("attachment_url")
    private String attachmentUrl;
}