package tools;

import org.hamcrest.Matcher;
import org.hamcrest.core.IsNull;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.hamcrest.Matchers.equalTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


public class GetRequestEquals {
    public static void performGetAndExpect(String url, Map<String, Object> expectedValues, MockMvc mockMvc) throws Exception {
        mockMvc.perform(get(url))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"));

        for (Map.Entry<String, Object> entry : expectedValues.entrySet()) {
            String jsonPathExpression = entry.getKey();
            Object expectedValue = entry.getValue();
            if (expectedValue instanceof Matcher) {
                mockMvc.perform(get(url))
                        .andExpect(jsonPath(jsonPathExpression, (Matcher<?>) expectedValue));
            } else if (expectedValue == null) {
                mockMvc.perform(get(url))
                        .andExpect(jsonPath(jsonPathExpression).value(IsNull.nullValue()));
            } else if (expectedValue.equals("DOES_NOT_EXIST")) {
                mockMvc.perform(get(url))
                        .andExpect(jsonPath(jsonPathExpression).doesNotExist());
            } else {
                mockMvc.perform(get(url))
                        .andExpect(jsonPath(jsonPathExpression, equalTo(expectedValue)));
            }
        }
    }
}
