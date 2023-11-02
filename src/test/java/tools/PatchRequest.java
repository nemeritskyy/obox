package tools;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.ResultActions;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;

public class PatchRequest {
    public static ResultActions performPatchRequest(String url, Map<String, Object> requestBody, MockMvc mockMvc) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonBody = objectMapper.writeValueAsString(requestBody);

        RequestBuilder request = patch(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonBody);

        return mockMvc.perform(request);
    }
}
