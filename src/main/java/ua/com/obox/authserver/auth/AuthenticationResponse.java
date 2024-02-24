package ua.com.obox.authserver.auth;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonPropertyOrder({"tenant_id", "access_token", "refresh_token"})
public class AuthenticationResponse {
  @JsonProperty("access_token")
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private String accessToken;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  @JsonProperty("refresh_token")
  private String refreshToken;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  @JsonProperty("tenant_id")
  private String tenantId;
}
