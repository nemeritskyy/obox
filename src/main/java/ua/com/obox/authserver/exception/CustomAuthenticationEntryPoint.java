package ua.com.obox.authserver.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import ua.com.obox.dbschema.tools.translation.CheckHeader;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {
    private static final ResourceBundle translation = ResourceBundle.getBundle("translation.messages");
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException {
        String acceptLanguage = request.getHeader("Accept-Language");
        String errorLanguage = CheckHeader.checkHeaderLanguage(acceptLanguage);
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType("application/json");

        Map<String, String> errorMap = new HashMap<>();
        errorMap.put("error", "AuthenticationFailed");
        errorMap.put("message", translation.getString(errorLanguage + ".badLoginOrPassword"));

        ObjectMapper objectMapper = new ObjectMapper();
        String jsonError = objectMapper.writeValueAsString(errorMap);

        byte[] bytes = jsonError.getBytes(StandardCharsets.UTF_8);
        response.getOutputStream().write(bytes);
    }
}