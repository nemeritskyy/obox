package ua.com.obox.dbschema.allergen;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AllergenResponseId {
    @JsonProperty("allergen_id")
    private String allergenId;
}
