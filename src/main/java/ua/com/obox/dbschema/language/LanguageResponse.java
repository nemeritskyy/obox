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
    @JsonProperty("restaurant_id")
    private String restaurant_id;
    @JsonProperty("name")
    private String name;
    @JsonProperty("label")
    private String label;
}