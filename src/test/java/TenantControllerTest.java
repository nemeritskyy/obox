import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import tools.GetRequestEquals;
import tools.PatchRequest;
import tools.PostRequest;
import ua.com.obox.OboxApplication;

import java.util.Map;
import java.util.UUID;

import static org.hamcrest.Matchers.equalTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = OboxApplication.class)
@ActiveProfiles({"local", "test"})
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TenantControllerTest {
    private final String URL_TENANT = "https://api.obox.com.ua/tenants/";
    private static String tenantId = null;
    private static String tenantForbiddenStateDisabled = null;
    private MockMvc mockMvc;

    @BeforeEach
    public void setup(WebApplicationContext wac) {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
    }

    @Test
    @Order(1)
    public void testCreateTenantForTest() throws Exception { // Create Tenant for test
        Map<String, Object> requestBody = Map.of(
                "name",
                "createTenantForRestaurantTest()",
                "language", "en-US");
        tenantId = PostRequest.performGetIdAfterPost("$.tenant_id", URL_TENANT, requestBody, mockMvc);
    }

    @Test
    @Order(10)
    public void testGetByExistingId() throws Exception {
        mockMvc.perform(get(URL_TENANT + tenantId)) // Get. 1
                .andExpect(status().isOk());
    }

    @Test
    @Order(11)
    public void testGetByNotFoundId() throws Exception { // Get. 2
        mockMvc.perform(get(URL_TENANT + UUID.randomUUID()))
                .andExpect(status().isNotFound());
    }

    @ParameterizedTest
    @Order(30)
    @Transactional
    @MethodSource("tools.TestValues#getValidNames")
    public void testPostValidNames(String name) throws Exception {
        Map<String, Object> requestBody = Map.of(
                "name", name,
                "language", "en-US");
        PostRequest
                .performPostRequest(URL_TENANT, requestBody, mockMvc)
                .andExpect(status().isCreated());
    }

    @ParameterizedTest
    @Order(40)
    @Transactional
    @MethodSource("tools.TestValues#getInvalidNames")
    public void testPostInvalidNames(String name) throws Exception {
        Map<String, Object> requestBody = Map.of(
                "name", name);
        PostRequest
                .performPostRequest(URL_TENANT, requestBody, mockMvc)
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(41)
    public void testPostSpaceSymbol() throws Exception { // Post. 7 // Get. 5
        String spaceName = "      New name with space  ";

        Map<String, Object> requestResponse;
        Map<String, Object> requestBody = Map.of(
                "name", spaceName,
                "language", "en-US");
        tenantForbiddenStateDisabled = PostRequest
                .performGetIdAfterPost("$.tenant_id", URL_TENANT, requestBody, mockMvc);

        requestResponse = Map.of(
                "$.tenant_id", equalTo(tenantForbiddenStateDisabled),
                "$.content.en-US.name", equalTo(spaceName.trim())
        );

        GetRequestEquals
                .performGetAndExpect(URL_TENANT + tenantForbiddenStateDisabled, requestResponse, mockMvc);

        mockMvc.perform(delete(URL_TENANT + tenantForbiddenStateDisabled)); // State Forbidden
    }

    @Test
    @Order(42)
    public void testGetForbidden() throws Exception {
        mockMvc.perform(get(URL_TENANT + tenantForbiddenStateDisabled)) // Get. 1.a
                .andExpect(status().isForbidden());
    }

    @ParameterizedTest
    @Order(50)
    @MethodSource("tools.TestValues#getValidNames") // Patch. 2
    public void testPatchValid(String name) throws Exception {
        Map<String, Object> requestResponse;
        Map<String, Object> requestBody = Map.of(
                "name", name,
                "language", "en-US");

        PatchRequest
                .performPatchRequest(URL_TENANT + tenantId, requestBody, mockMvc)
                .andExpect(status().isNoContent());

        requestResponse = Map.of(
                "$.tenant_id", equalTo(tenantId),
                "$.content.en-US.name", equalTo(name.trim())
        );

        GetRequestEquals
                .performGetAndExpect(URL_TENANT + tenantId, requestResponse, mockMvc);
    }

    @ParameterizedTest
    @Order(51)
    @MethodSource("tools.TestValues#getInvalidNames") // Patch. 3 // Patch. 4
    public void testPatchInvalid(String name) throws Exception {
        Map<String, Object> requestBody = Map.of(
                "name", name);
        PatchRequest
                .performPatchRequest(URL_TENANT + tenantId, requestBody, mockMvc)
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(52)
    public void testGetByIdAfterPatch() throws Exception { // Get. 3 Patch. 1
        String name = "Patch name";
        Map<String, Object> requestResponse;
        Map<String, Object> requestBody = Map.of(
                "name", name,
                "language", "en-US");
        PatchRequest
                .performPatchRequest(URL_TENANT + tenantId, requestBody, mockMvc)
                .andExpect(status().isNoContent());

        requestResponse = Map.of(
                "$.content.en-US.name", equalTo(name.trim())
        );

        GetRequestEquals
                .performGetAndExpect(URL_TENANT + tenantId, requestResponse, mockMvc);
    }


    @Test
    public void testPatchByIdRandomSymbol() throws Exception { // Patch. 5
        String name = "Patch name";
        Map<String, Object> requestBody = Map.of(
                "name", name);
        PatchRequest
                .performPatchRequest(URL_TENANT + tenantId + RandomStringUtils.random(1, true, true), requestBody, mockMvc)
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(90)
    public void testDeleted() throws Exception { // Del. 1
        String name = "Patch name";
        Map<String, Object> requestBody = Map.of(
                "name", name);

        mockMvc.perform(delete(URL_TENANT + tenantId + "?forceDelete=true")).andExpect(status().isNoContent());

        mockMvc.perform(delete(URL_TENANT + tenantForbiddenStateDisabled + "?forceDelete=true"));

        PatchRequest
                .performPatchRequest(URL_TENANT + tenantId + RandomStringUtils.random(1, true, true), requestBody, mockMvc)
                .andExpect(status().isNotFound());
    }

    //
    @Test
    @Order(90)
    public void testDeleteRandomSymbol() throws Exception { // Del. 4
        String randomSymbol = RandomStringUtils.random(1, true, true);
        mockMvc.perform(delete(URL_TENANT + randomSymbol)).andExpect(status().isNotFound());
    }

    @Test
    @Order(100)
    public void testGetAfterDelete() throws Exception { // Get. 2.b // Get. 4
        mockMvc.perform(get(URL_TENANT + tenantId)).andExpect(status().isNotFound());
    }
}