import com.jayway.jsonpath.JsonPath;
import org.apache.commons.lang3.RandomStringUtils;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import tools.TestValues;
import ua.com.obox.OboxApplication;
import ua.com.obox.dbschema.tenant.*;

import java.util.UUID;

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
    private final String URL = "https://api.obox.com.ua/tenants/";
    private static final String tenantTestIdExisting = "972c6465-200d-4ec3-ab1c-aaa359cdab35"; // must be really row in db
    //    private static final String tenantTestIdExisting = "e93614c7-e58e-40b6-84b2-01c4f5055fc9"; // must be really row in db
    private static final String tenantForbiddenStateDisabled = "519e21ef-83ab-46ab-afb6-a1460963a2df"; // must be really row in db state DISABLED
    //    private static final String tenantForbiddenStateDisabled = "a95de739-40fa-414a-9f62-fdaedb2a8282"; // must be really row in db state DISABLED
    private static String tenantId = null;

    @Autowired
    private MockMvc mockMvc;
    @Mock
    private TenantRepository tenantRepository;

    @BeforeEach
    public void setup(WebApplicationContext wac) {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
    }

    @ParameterizedTest
    @MethodSource("tools.TestValues#getValidNames")
    public void testPostValid(String name) {
        assertAll(
                () -> TestValues.testValidNameInRequest(name, post(URL), mockMvc)
        );
    }

    @ParameterizedTest
    @MethodSource("tools.TestValues#getInvalidNames")
    public void testPostInvalid(String name) {
        assertAll(
                () -> TestValues.testInvalidNameInRequest(name, post(URL), mockMvc)
        );
    }

    @ParameterizedTest
    @MethodSource("tools.TestValues#getValidNames") // Patch. 2
    public void testPatchValid(String name) throws Exception {
        String jsonBody = "{\"name\": \"" + name + "\"}";
        mockMvc.perform(patch(URL + tenantTestIdExisting)
                        .contentType("application/json")
                        .content(jsonBody))
                .andExpect(status().isNoContent());

        mockMvc.perform(get(URL + tenantTestIdExisting))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.tenant_id").exists())
                .andExpect(jsonPath("$.name", equalTo(name.trim())));
    }

    @ParameterizedTest
    @MethodSource("tools.TestValues#getInvalidNames") // Patch. 3 // Patch. 4
    public void testPatchInvalid(String name) throws Exception {
        String jsonBody = "{\"name\": \"" + name + "\"}";
        mockMvc.perform(patch(URL + tenantTestIdExisting)
                        .contentType("application/json")
                        .content(jsonBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testPostSpaceSymbol() throws Exception { // Post. 7 // Get. 5
        String newName = "      New name  ";
        String jsonBody = "{\"name\": \"" + newName + "\"}";

        MvcResult result = mockMvc.perform(post(URL)
                        .contentType("application/json")
                        .content(jsonBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tenant_id", Matchers.notNullValue()))
                .andReturn();

        String responseJson = result.getResponse().getContentAsString();
        tenantId = JsonPath.parse(responseJson).read("$.tenant_id", String.class);

        mockMvc.perform(get(URL + tenantId))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.tenant_id", equalTo(tenantId)))
                .andExpect(jsonPath("$.name", equalTo(newName.trim())));
    }

    @Test
    public void testGetByExistingId() throws Exception {
        mockMvc.perform(get(URL + tenantTestIdExisting)) // Get. 1
                .andExpect(status().isOk());
    }

    @Test
    public void testGetByNotFoundId() throws Exception { // Get. 2
        mockMvc.perform(get(URL + RandomStringUtils.random(5, true, true)))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testGetByIdAfterPatch() throws Exception { // Get. 3 Patch. 1
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
    public void testGetByInvalidId() throws Exception { // Get. 4
        mockMvc.perform(get(URL + UUID.randomUUID()))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testGetForbidden() throws Exception {
        mockMvc.perform(get(URL + tenantForbiddenStateDisabled)) // Get. 1.a
                .andExpect(status().isForbidden());
    }

    @Test
    public void testPatchByIdRandomSymbol() throws Exception { // Patch. 5
        String newName = "New name";
        String jsonBody = "{\"name\": \"" + newName + "\"}";
        String randomSymbol = RandomStringUtils.random(1, true, true);
        mockMvc.perform(patch(URL + randomSymbol)
                        .contentType("application/json")
                        .content(jsonBody))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testPatchDeleted() throws Exception { // Patch. 6 // Del. 1
        mockMvc.perform(delete(URL + tenantId + "?forceDelete=true")).andExpect(status().isNoContent());
        String newName = "New name";
        String jsonBody = "{\"name\": \"" + newName + "\"}";
        mockMvc.perform(patch(URL + tenantId)
                        .contentType("application/json")
                        .content(jsonBody))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testDeleteRandomSymbol() throws Exception { // Del. 4
        String randomSymbol = RandomStringUtils.random(1, true, true);
        mockMvc.perform(delete(URL + randomSymbol)).andExpect(status().isNotFound());
    }

    @Test
    public void testGetAfterDelete() throws Exception { // Get. 2.b
        mockMvc.perform(get(URL + tenantId)).andExpect(status().isNotFound());
    }

//    Code Example
//    @ParameterizedTest
//    @ValueSource(strings = {"", "    ", "clakxliwqxrun"})
}