package ua.com.obox.authserver.exception;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
public class CustomErrorHandler {
    public static void handleException(HttpServletResponse response, Exception ex, HttpStatus httpStatus) throws IOException {
        response.setStatus(httpStatus.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        Map<String, String> errorMap = new HashMap<>();
        errorMap.put("error", ex.getClass().getSimpleName());
        errorMap.put("message", ex.getMessage());
        errorMap.put("time", String.valueOf(LocalDateTime.now()));

        ObjectMapper objectMapper = new ObjectMapper();
        String jsonError = objectMapper.writeValueAsString(errorMap);

        response.getWriter().write(jsonError);
    }
}
