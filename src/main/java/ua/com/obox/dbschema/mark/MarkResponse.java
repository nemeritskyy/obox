package ua.com.obox.dbschema.mark;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ua.com.obox.dbschema.translation.assistant.OnlyName;
import ua.com.obox.dbschema.translation.responsebody.Content;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonPropertyOrder({"mark_id", "translation_id", "content"})
public class MarkResponse {
    @JsonProperty("mark_id")
    private String markId;
    @JsonProperty("translation_id")
    private String translationId;
    @JsonUnwrapped
    private Content<OnlyName> content;
}
