package ua.com.obox.dbschema.tenant;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TenantResponse {
    @JsonProperty("tenant_id")
    private String tenantId;
    @JsonProperty("name")
    private String name;
}
