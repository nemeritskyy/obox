package tools;

import org.apache.commons.lang3.RandomStringUtils;
import org.json.JSONObject;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.stream.Stream;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class TestValues {
    private static Stream<String> getValidNames() {
        return Stream.of(
                "a", // Post. 1
//                "A", // Post. 3
//                " A",
                "Ї", // Post. 4
                "3", // Post. 5
                "&",
                RandomStringUtils.random(199, true, true) // Post. 8
        );
    }

    private static Stream<String> getInvalidNames() {
        return Stream.of(
                "    ", // Post. 6
                RandomStringUtils.random(202, true, true), // Post. 9
                "" // Post. 10
        );
    }

    public static void testInvalidNameInRequest(String name, MockHttpServletRequestBuilder requestBuilder, MockMvc mockMvc) throws Exception {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("name", name);

        mockMvc.perform(requestBuilder
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonObject.toString()))
                .andExpect(status().isBadRequest());
    }

    public static void testValidNameInRequest(String name, MockHttpServletRequestBuilder requestBuilder, MockMvc mockMvc) throws Exception {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("name", name);

        mockMvc.perform(requestBuilder
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonObject.toString()))
                .andExpect(status().isCreated());
    }
}
