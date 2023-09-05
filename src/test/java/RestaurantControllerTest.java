import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = OboxApplication.class)
@ActiveProfiles("local")
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class RestaurantControllerTest {
    private final String URL_TENANT = "https://api.obox.com.ua/tenants/";
    private final String URL_RESTAURANT = "https://api.obox.com.ua/restaurants/";
    private static String tenantId = null;
    private static String restaurantId = null;

    @Autowired
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
                "createTenantForRestaurantTest()");

        tenantId = PostRequest.performGetIdAfterPost("$.tenant_id", URL_TENANT, requestBody, mockMvc);
    }

    @Test
    @Order(10)
    @Transactional
    public void testTenantInvalidUUID() throws Exception { // Post. 1
        Map<String, Object> requestBody = Map.of(
                "tenant_id", UUID.randomUUID(),
                "name", "RestaurantName");
        PostRequest
                .performPostRequest(URL_RESTAURANT, requestBody, mockMvc)
                .andExpect(status().isBadRequest());
    }

    @ParameterizedTest
    @Order(20)
    @MethodSource("tools.TestValues#getValidNames")
    public void testPostValidNames(String name) throws Exception {
        Map<String, Object> requestBody = Map.of(
                "tenant_id", tenantId,
                "name", name);
        PostRequest
                .performPostRequest(URL_RESTAURANT, requestBody, mockMvc)
                .andExpect(status().isCreated());
    }

    @ParameterizedTest
    @Order(21)
    @MethodSource("tools.TestValues#getInvalidNames")
    public void testPostInvalidNames(String name) throws Exception { // Post. 2 // Post. 3 // Post. 4
        Map<String, Object> requestBody = Map.of(
                "tenant_id", tenantId,
                "name", name);
        PostRequest
                .performPostRequest(URL_RESTAURANT, requestBody, mockMvc)
                .andExpect(status().isBadRequest());
    }

    @ParameterizedTest
    @Order(22)
    @MethodSource("tools.values.TestValuesRestaurant#getValidAddress")
    public void testPostValidAddress(String validAddress) throws Exception { // Post. 5
        Map<String, Object> requestBody = Map.of(
                "tenant_id", tenantId,
                "name", "RestaurantName",
                "address", validAddress);

        restaurantId = PostRequest.performGetIdAfterPost("$.restaurant_id", URL_RESTAURANT, requestBody, mockMvc); // Create Restaurant for test
    }

    @ParameterizedTest
    @Order(23)
    @MethodSource("tools.values.TestValuesRestaurant#getInvalidAddress")
    public void testPostInvalidAddress(String invalidAddress) throws Exception { // Post. 6
        Map<String, Object> requestBody = Map.of(
                "tenant_id", tenantId,
                "name", "RestaurantName",
                "address", invalidAddress);
        PostRequest
                .performPostRequest(URL_RESTAURANT, requestBody, mockMvc)
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(24)
    public void testPostTenantIdWithSpace() throws Exception { // Post. 8
        Map<String, Object> requestBody = Map.of(
                "tenant_id", tenantId + " ", // add space
                "name", "RestaurantName");
        PostRequest
                .performPostRequest(URL_RESTAURANT, requestBody, mockMvc)
                .andExpect(status().isCreated());
    }

    @Test
    @Order(35)
    public void testGetInvalidId() throws Exception { // Get. 1
        mockMvc.perform(get(URL_RESTAURANT + UUID.randomUUID()))
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(36)
    public void testEmptyAddressSpaceBeforeName() throws Exception { // Get. 2 address null // Get. 3
        Map<String, Object> requestResponse;
        String testName = " RestaurantName"; // with space
        String testRestaurantId = null;
        Map<String, Object> requestBody = Map.of(
                "tenant_id", tenantId, //
                "name", testName);
        testRestaurantId = PostRequest.performGetIdAfterPost("$.restaurant_id", URL_RESTAURANT, requestBody, mockMvc);

        requestResponse = Map.of(
                "$.name", equalTo(testName.trim()),
                "$.address", "DOES_NOT_EXIST"
        );
        GetRequestEquals.performGetAndExpect(URL_RESTAURANT + testRestaurantId, requestResponse, mockMvc);
    }

    @Test
    @Order(45)
    public void testPatchInvalidIdRestaurant() throws Exception { // Patch. 1
        String name = "Restaurant JUnit new";
        Map<String, Object> requestBody = Map.of(
                "name", name);
        PatchRequest
                .performPatchRequest(URL_RESTAURANT + UUID.randomUUID(), requestBody, mockMvc)
                .andExpect(status().isNotFound());
    }

    @ParameterizedTest
    @Order(46)
    @MethodSource("tools.values.TestValuesRestaurant#getInvalidAddress")
    public void testPatchInvalidAddress(String invalidAddress) throws Exception { // Patch. 2
        Map<String, Object> requestBody = Map.of(
                "address", invalidAddress);
        PatchRequest
                .performPatchRequest(URL_RESTAURANT + restaurantId, requestBody, mockMvc)
                .andExpect(status().isBadRequest());
    }

    @ParameterizedTest
    @Order(47)
    @MethodSource("tools.TestValues#getInvalidNames")
    public void testPatchEmptyAndSpaceName() throws Exception { // Patch. 3 // Patch. 4 // Patch. 5
        Map<String, Object> requestBody = Map.of(
                "name", "");
        PatchRequest
                .performPatchRequest(URL_RESTAURANT + restaurantId, requestBody, mockMvc)
                .andExpect(status().isBadRequest());
    }


    @Test
    @Order(50)
    public void testPatchExistingRestaurant() throws Exception { // Patch. 6
        String name = "Restaurant JUnit new";
        Map<String, Object> requestBody = Map.of(
                "name", name);
        PatchRequest
                .performPatchRequest(URL_RESTAURANT + restaurantId, requestBody, mockMvc)
                .andExpect(status().isNoContent());
    }

    @Test
    @Order(65)
    public void testDeleteRandomUUID() throws Exception { // Del. 1
        mockMvc.perform(delete(URL_TENANT + UUID.randomUUID() + "?forceDelete=true"))
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(70)
    public void testDeleteTemporaryTenant() throws Exception {
        mockMvc.perform(delete(URL_TENANT + tenantId + "?forceDelete=true"))
                .andExpect(status().isNoContent());
    }
}
