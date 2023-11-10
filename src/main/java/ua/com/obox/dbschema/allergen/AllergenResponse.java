package ua.com.obox.dbschema.allergen;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ua.com.obox.dbschema.translation.assistant.ExistName;
import ua.com.obox.dbschema.translation.responsebody.Content;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonPropertyOrder({"allergen_id", "translation_id", "content"})
public class AllergenResponse {
    @JsonProperty("allergen_id")
    private String allergenId;
    @JsonProperty("translation_id")
    private String translationId;
    @JsonUnwrapped
    private Content<ExistName> content;
}
