package ua.com.obox.dbschema.tenant;

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
public class TenantResponse {
    @JsonProperty("tenant_id")
    private String tenantId;
    @JsonProperty("content")
    private Content content;
}
