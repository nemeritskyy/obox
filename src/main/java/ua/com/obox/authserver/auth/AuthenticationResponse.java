package ua.com.obox.authserver.auth;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthenticationResponse {
  @JsonProperty("access_token")
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private String accessToken;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  @JsonProperty("refresh_token")
  private String refreshToken;
}
