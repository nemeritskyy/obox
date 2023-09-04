import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import tools.GetRequestEquals;
import tools.PatchRequest;
import tools.PostRequest;
import ua.com.obox.OboxApplication;

import java.util.Map;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = OboxApplication.class)
@ActiveProfiles("local")
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class MenuControllerTest {
    private final String URL_TENANT = "http://localhost/tenants/";
    private final String URL_RESTAURANT = "http://localhost/restaurants/";
    private final String URL_MENU = "http://localhost/menus/";
    private final String URL_ASSOCIATED = "http://localhost/associated/";
    private static String tenantId = null;
    private static String restaurantId = null;
    private static String menuId = null;
    private static String languageCode = "ua";

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
                "name", "createTenantForMenuTest()");

        tenantId = PostRequest.performGetIdAfterPost("$.tenant_id", URL_TENANT, requestBody, mockMvc);
    }

    @ParameterizedTest
    @Order(2)
    @MethodSource("tools.values.TestValuesRestaurant#getValidAddress")
    public void testCreateRestaurantForTest(String validAddress) throws Exception { // Post. 5
        Map<String, Object> requestBody = Map.of(
                "tenant_id", tenantId,
                "name", "createRestaurantForMenuTest()",
                "address", validAddress);

        restaurantId = PostRequest.performGetIdAfterPost("$.restaurant_id", URL_RESTAURANT, requestBody, mockMvc); // Create Restaurant for test
        System.out.println(restaurantId);
    }

    @Test
    @Order(3)
    public void testMenuForTest() throws Exception { // Create Menu for test name with space and get equals
        Map<String, Object> requestResponse;
        String nameSpace = "    New Space Name  ";
        Map<String, Object> requestBody = Map.of(
                "restaurant_id", restaurantId,
                "name", nameSpace,
                "language_code", languageCode
        );

        menuId = PostRequest.performGetIdAfterPost("$.menu_id", URL_MENU, requestBody, mockMvc);

        requestResponse = Map.of(
                "$.menu_id", equalTo(menuId),
                "$.restaurant_id", equalTo(restaurantId),
                "$.name", equalTo(nameSpace.trim()),
                "$.language_code", equalTo(languageCode)
        );

        GetRequestEquals.performGetAndExpect(URL_MENU + menuId, requestResponse, mockMvc);
    }

    @ParameterizedTest
    @Order(10)
    @MethodSource("tools.TestValues#getValidNames")
    // Post. 1 // Post. 2 //  Post. 3 //  Post. 4 // Post. 5 // Post. 8 // Post. (10)11
    public void testPostValidNames(String name) throws Exception {
        Map<String, Object> requestBody = Map.of(
                "restaurant_id", restaurantId,
                "name", name,
                "language_code", languageCode
        );
        PostRequest
                .performPostRequest(URL_MENU, requestBody, mockMvc)
                .andExpect(status().isCreated());
    }

    @ParameterizedTest
    @Order(10)
    @MethodSource("tools.TestValues#getInvalidNames")
    public void testPostInvalidNames(String name) throws Exception { // Post. 6 // Post. 12
        Map<String, Object> requestBody = Map.of(
                "restaurant_id", restaurantId,
                "name", name,
                "language_code", languageCode
        );
        PostRequest
                .performPostRequest(URL_MENU, requestBody, mockMvc)
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(11)
    public void testPostInvalidRestaurantId() throws Exception {
        Map<String, Object> requestBody = Map.of(
                "restaurant_id", UUID.randomUUID(),
                "name", "createMenuForMenuTest()",
                "language_code", languageCode
        );
        PostRequest
                .performPostRequest(URL_MENU, requestBody, mockMvc)
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(30)
    public void testPatchExistingMenu() throws Exception { // Patch. 1
        String name = "Menu JUnit new";
        Map<String, Object> requestBody = Map.of(
                "name", name);
        PatchRequest
                .performPatchRequest(URL_MENU + menuId, requestBody, mockMvc)
                .andExpect(status().isNoContent());
    }

    @ParameterizedTest
    @Order(35)
    @MethodSource("tools.TestValues#getInvalidNames") // Patch. 2 // Patch. 5 // Patch. 6
    public void testPatchInvalidNames(String name) throws Exception {
        Map<String, Object> requestBody = Map.of(
                "name", name
        );
        PatchRequest
                .performPatchRequest(URL_MENU + menuId, requestBody, mockMvc)
                .andExpect(status().isBadRequest());
    }

    @ParameterizedTest
    @Order(40)
    @MethodSource("tools.TestValues#getValidNames") // Patch. 3 // Patch. 4
    public void testPatchValidNames(String name) throws Exception {
        Map<String, Object> requestBody = Map.of(
                "name", name+"1"
        );
        PatchRequest
                .performPatchRequest(URL_MENU + menuId, requestBody, mockMvc)
                .andExpect(status().isNoContent());
    }

    @Test
    @Order(50)
    public void testPatchInvalidId() throws Exception { // Patch. 7
        Map<String, Object> requestBody = Map.of(
                "name", "name"
        );
        PatchRequest
                .performPatchRequest(URL_MENU + UUID.randomUUID(), requestBody, mockMvc)
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(60)
    public void testGetAfterPatch() throws Exception { // Get. 1
        Map<String, Object> requestResponse = Map.of(
                "$.menu_id", notNullValue(),
                "$.restaurant_id", notNullValue(),
                "$.name", notNullValue(),
                "$.language_code", notNullValue()
        );
        GetRequestEquals.performGetAndExpect(URL_MENU + menuId, requestResponse, mockMvc);
    }

    @Test
    @Order(61)
    public void testGetByInvalidId() throws Exception { // Get. 3
        mockMvc.perform(get(URL_MENU + UUID.randomUUID()))
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(98)
    public void testDeleteMenu() throws Exception { // Del. 2
        mockMvc.perform(delete(URL_MENU + menuId))
                .andExpect(status().isNoContent());
    }

    @Test
    @Order(99)
    public void testDeleteTemporaryAndAssociatedDataTenant() throws Exception {
        mockMvc.perform(delete(URL_ASSOCIATED + restaurantId)); // delete associated data for test
        System.out.println(URL_ASSOCIATED + restaurantId);
        mockMvc.perform(delete(URL_TENANT + tenantId + "?forceDelete=true"))
                .andExpect(status().isNoContent());
    }

    @Test
    @Order(120)
    public void testGetAfterDeleting() throws Exception { // Get. 2 // Del. 1
        mockMvc.perform(get(URL_MENU + menuId))
                .andExpect(status().isNotFound());
    }
}