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
import static org.hamcrest.Matchers.equalTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = OboxApplication.class)
@ActiveProfiles("local")
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CategoryControllerTest {
    private final String URL_TENANT = "http://localhost/tenants/";
    private final String URL_RESTAURANT = "http://localhost/restaurants/";
    private final String URL_MENU = "http://localhost/menus/";
    private final String URL_CATEGORY = "http://localhost/categories/";
    private final String URL_ASSOCIATED = "http://localhost/associated/";
    private static String tenantId = null;
    private static String restaurantId = null;
    private static String menuId = null;
    private static String categoryId = null;
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
    public void testCreateRestaurantForTest(String validAddress) throws Exception { // Create Restaurant for test
        Map<String, Object> requestBody = Map.of(
                "tenant_id", tenantId,
                "name", "createRestaurantForMenuTest()",
                "address", validAddress);

        restaurantId = PostRequest.performGetIdAfterPost("$.restaurant_id", URL_RESTAURANT, requestBody, mockMvc);
    }

    @Test
    @Order(3)
    public void testMenuTenantForTest() throws Exception { // Create Menu for test
        Map<String, Object> requestBody = Map.of(
                "restaurant_id", restaurantId,
                "name", "MenuForCategoryTest",
                "language_code", languageCode,
                "state","ENABLED"
        );

        menuId = PostRequest.performGetIdAfterPost("$.menu_id", URL_MENU, requestBody, mockMvc);
    }

    @Test
    @Order(4)
    public void testCategoryForTest() throws Exception { // Create Category for test // Post. 6 // Post. 7 // Get. 2
        Map<String, Object> responseJson;
        String nameSpace = "    New Space Name  ";
        Map<String, Object> requestBody = Map.of(
                "menu_id", menuId,
                "name", nameSpace,
                "state","ENABLED"
        );

        categoryId = PostRequest.performGetIdAfterPost("$.category_id", URL_CATEGORY, requestBody, mockMvc);

        responseJson = Map.of(
                "menu_id", notNullValue(),
                "category_id", equalTo(categoryId),
                "name", equalTo(nameSpace.trim())
        );

        GetRequestEquals.performGetAndExpect(URL_CATEGORY + categoryId, responseJson, mockMvc);
    }

    @Test
    @Order(20)
    public void testPostInvalidMenuId() throws Exception {
        Map<String, Object> requestBody = Map.of(
                "menu_id", UUID.randomUUID(),
                "name", "CategoryTest"
        );
        PostRequest
                .performPostRequest(URL_CATEGORY, requestBody, mockMvc)
                .andExpect(status().isBadRequest());
    }

    @ParameterizedTest
    @Order(21)
    @MethodSource("tools.TestValues#getInvalidNames")
    public void testPostInvalidNames(String name) throws Exception { // Post. 2 // Post. 3 // Post. 4
        Map<String, Object> requestBody = Map.of(
                "menu_id", menuId,
                "name", name
        );
        PostRequest.performPostRequest(URL_CATEGORY, requestBody, mockMvc)
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(50)
    public void testGetInvalidId() throws Exception { // Get. 1
        mockMvc.perform(get(URL_CATEGORY + UUID.randomUUID()))
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(60)
    public void testPatchNonExistingId() throws Exception { // Patch. 1
        Map<String, Object> requestBody = Map.of(
                "name", "name"
        );
        PatchRequest.performPatchRequest(URL_CATEGORY + UUID.randomUUID(), requestBody, mockMvc)
                .andExpect(status().isNotFound());
    }

    @ParameterizedTest
    @Order(61)
    @MethodSource("tools.TestValues#getInvalidNames")
    public void testPatchInvalidNames(String name) throws Exception { // Patch. 3 // Patch. 4 // Patch. 5
        Map<String, Object> requestBody = Map.of(
                "name", name
        );
        PatchRequest.performPatchRequest(URL_CATEGORY + categoryId, requestBody, mockMvc)
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(70)
    public void testDeleteNonExisting() throws Exception { // Del. 1
        mockMvc.perform(delete(URL_CATEGORY + UUID.randomUUID()))
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(99)
    public void testDeleteTemporaryAndAssociatedDataTenant() throws Exception {
        mockMvc.perform(delete(URL_ASSOCIATED + restaurantId)); // delete associated data for test
        mockMvc.perform(delete(URL_TENANT + tenantId + "?forceDelete=true"))
                .andExpect(status().isNoContent());
    }
}
