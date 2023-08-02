import org.apache.commons.lang3.RandomStringUtils;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import ua.com.obox.OboxApplication;
import ua.com.obox.dbschema.tenant.*;

import java.util.UUID;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = OboxApplication.class)
@AutoConfigureMockMvc
@Transactional
public class TenantControllerTest {
    private final String URL = "http://localhost:8080/tenants/";
    private static final String tenantTestIdExisting = "e93614c7-e58e-40b6-84b2-01c4f5055fc9"; // must be really row in db
    private static final String tenantForbiddenStateDisabled = "a95de739-40fa-414a-9f62-fdaedb2a8282"; // must be really row in db state DISABLED
    @Autowired
    private MockMvc mockMvc;
    @Mock
    private TenantRepository tenantRepository;

    @BeforeEach
    public void setup(WebApplicationContext wac) {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
    }

    @ParameterizedTest
    @MethodSource("getValidNames")
    public void testPostSuccessCreateTenant(String name) {
        assertAll(
                () -> testValidNameInRequest(name, post(URL))
        );
    }

    private static Stream<String> getValidNames() {
        return Stream.of(
                "a", // Post. 1
                "A", // Post. 3
                "Ї", // Post. 4
                "3", // Post. 5
                RandomStringUtils.random(200, true, true) // Post. 8
        );
    }

    @ParameterizedTest
    @MethodSource("getInvalidNames")
    public void testCreateAndPatchNameEmptySpace201Chars(String name) { //Bad Request
        assertAll(
                () -> testInvalidNameInRequest(name, post(URL)),
                () -> testInvalidNameInRequest(name, patch(URL + tenantTestIdExisting))
        );
    }

    private static Stream<String> getInvalidNames() {
        return Stream.of(
                "    ", // Post. 6
                RandomStringUtils.random(202, true, true), // Post. 9
                "" // Post. 10
        );
    }

    @Test
    public void testGetTenantByExistingId() throws Exception {
        mockMvc.perform(get(URL + tenantTestIdExisting)) // Get. 1
                .andExpect(status().isOk());
    }

    @Test
    public void testGetTenantByNotFoundId() throws Exception { // Get. 2
        mockMvc.perform(get(URL + RandomStringUtils.random(5, true, true)))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testGetTenantByIdAfterPatch() throws Exception { // Get 3
        String newName = "New name";
        String jsonBody = "{\"name\": \"" + newName + "\"}";

        mockMvc.perform(patch(URL + tenantTestIdExisting)
                        .contentType("application/json")
                        .content(jsonBody))
                .andExpect(status().isNoContent());

        mockMvc.perform(get(URL + tenantTestIdExisting))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.tenant_id").exists())
                .andExpect(jsonPath("$.name", equalTo(newName)));
    }

    @Test
    public void testGetTenantByInvalidId() throws Exception { // Get. 4
        mockMvc.perform(get(URL + UUID.randomUUID()))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testGetTenantForbidden() throws Exception {
        mockMvc.perform(get(URL + tenantForbiddenStateDisabled)) // Get. 1.a
                .andExpect(status().isForbidden());
    }

    private void testInvalidNameInRequest(String name, MockHttpServletRequestBuilder requestBuilder) throws Exception {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("name", name);

        mockMvc.perform(requestBuilder
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonObject.toString()))
                .andExpect(status().isBadRequest());
    }

    private void testValidNameInRequest(String name, MockHttpServletRequestBuilder requestBuilder) throws Exception {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("name", name);

        mockMvc.perform(requestBuilder
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonObject.toString()))
                .andExpect(status().isCreated());
    }
//    Code Example
//    @ParameterizedTest
//    @ValueSource(strings = {"", "    ", "clakxliwqxrun"})
}