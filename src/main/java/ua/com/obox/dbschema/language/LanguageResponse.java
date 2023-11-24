package ua.com.obox.dbschema.language;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LanguageResponse {
    @JsonProperty("language_id")
    private String languageId;
    @JsonProperty("name")
    private String name;
    @JsonProperty("label")
    private String label;
}