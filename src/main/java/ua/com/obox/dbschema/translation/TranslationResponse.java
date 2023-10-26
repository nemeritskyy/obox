package ua.com.obox.dbschema.translation;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ua.com.obox.dbschema.translation.responsebody.Content;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonPropertyOrder({"translation_id", "reference_id", "reference_type", "content"})
public class TranslationResponse {
    @JsonProperty("translation_id")
    private String translationId;
    @JsonProperty("reference_id")
    private String referenceId;
    @JsonProperty("reference_type")
    private String referenceType;
    @JsonUnwrapped
    private Content content;
}