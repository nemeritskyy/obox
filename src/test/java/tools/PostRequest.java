package tools;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import org.hamcrest.Matchers;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.ResultActions;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


public class PostRequest {
    public static ResultActions performPostRequest(String url, Map<String, Object> requestBody, MockMvc mockMvc) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonBody = objectMapper.writeValueAsString(requestBody);

        RequestBuilder request = post(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonBody);

        return mockMvc.perform(request);
    }

    public static String performGetIdAfterPost(String path, String url, Map<String, Object> requestBody, MockMvc mockMvc) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonBody = objectMapper.writeValueAsString(requestBody);
        String returnId = null;

        RequestBuilder request = post(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonBody);

        String responseJson = mockMvc.perform(request).andExpect(status().isCreated())
                .andExpect(jsonPath(path, Matchers.notNullValue()))
                .andReturn().getResponse().getContentAsString();

        returnId = JsonPath.parse(responseJson).read(path, String.class);

        return returnId;
    }
}
