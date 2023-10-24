package ua.com.obox.dbschema.translation;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ua.com.obox.dbschema.translation.responsebody.Content;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TranslationResponse {
    @JsonProperty("translation_id")
    private String translationId;
    @JsonProperty("reference_id")
    private String referenceId;
    @JsonProperty("reference_type")
    private String referenceType;
    @JsonProperty("content")
    private Content content;
}