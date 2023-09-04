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
import tools.values.SupportingData;
import ua.com.obox.OboxApplication;

import java.util.Map;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = OboxApplication.class)
@ActiveProfiles("local")
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DishControllerTest {
    private final String URL_TENANT = "http://localhost/tenants/";
    private final String URL_RESTAURANT = "http://localhost/restaurants/";
    private final String URL_MENU = "http://localhost/menus/";
    private final String URL_CATEGORY = "http://localhost/categories/";
    private final String URL_ASSOCIATED = "http://localhost/associated/";
    private final String URL_DISH = "http://localhost/dishes/";
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
                "name", "createTenantForDishTest()");

        tenantId = PostRequest.performGetIdAfterPost("$.tenant_id", URL_TENANT, requestBody, mockMvc);
    }

    @ParameterizedTest
    @Order(2)
    @MethodSource("tools.values.TestValuesRestaurant#getValidAddress")
    public void testCreateRestaurantForTest(String validAddress) throws Exception { // Create Restaurant for test
        Map<String, Object> requestBody = Map.of(
                "tenant_id", tenantId,
                "name", "createRestaurantForDishTest()",
                "address", validAddress);

        restaurantId = PostRequest.performGetIdAfterPost("$.restaurant_id", URL_RESTAURANT, requestBody, mockMvc);
    }

    @Test
    @Order(3)
    public void testMenuTenantForTest() throws Exception { // Create Menu for test
        Map<String, Object> requestBody = Map.of(
                "restaurant_id", restaurantId,
                "name", "MenuForDishTest",
                "language_code", languageCode
        );

        menuId = PostRequest.performGetIdAfterPost("$.menu_id", URL_MENU, requestBody, mockMvc);
    }

    @Test
    @Order(4)
    public void testCategoryForTest() throws Exception { // Create Category for test // Post. 6 // Post. 7 // Get. 2
        Map<String, Object> requestBody = Map.of(
                "menu_id", menuId,
                "name", "MenuForDishTest"
        );
        SupportingData.categoryId = PostRequest.performGetIdAfterPost("$.category_id", URL_CATEGORY, requestBody, mockMvc);
    }

    @Test
    @Order(19)
    public void testPostDishGetId() throws Exception {
        Map<String, Object> requestBody = Map.of(
                "category_id", SupportingData.categoryId,
                "name", "dish for test",
                "description", "desc dish for test",
                "price", 1,
                "weight", 1,
                "calories", 1,
                "state", "ENABLED"
        );
        SupportingData.dishId = PostRequest.performGetIdAfterPost("$.dish_id", URL_DISH, requestBody, mockMvc);
    }

    @ParameterizedTest
    @Order(20)
    @MethodSource("tools.values.TestValuesDishes#getValidPostRequests")
    public void testPostWithValidData201Created(Map<String, Object> requestBody) throws Exception {
        PostRequest.performPostRequest(URL_DISH, requestBody, mockMvc)
                .andExpect(status().isCreated());
    }

    @ParameterizedTest
    @Order(21)
    @MethodSource("tools.values.TestValuesDishes#getInvalidPostRequests")
    public void testPostWithInvalidData400BadRequest(Map<String, Object> requestBody) throws Exception {
        PostRequest.performPostRequest(URL_DISH, requestBody, mockMvc)
                .andExpect(status().isBadRequest());
    }

    @ParameterizedTest
    @Order(30)
    @MethodSource("tools.values.TestValuesDishes#getValidPatchRequests")
    public void testPatchWithValidData204NoContent(Map<String, Object> requestBody) throws Exception {
        PatchRequest.performPatchRequest(URL_DISH + SupportingData.dishId, requestBody, mockMvc)
                .andExpect(status().isNoContent());
    }

    @ParameterizedTest
    @Order(35)
    @MethodSource("tools.values.TestValuesDishes#getInvalidPatchRequests")
    public void testPatchInvalid400BadRequest(Map<String, Object> requestBody) throws Exception {
        PatchRequest.performPatchRequest(URL_DISH + SupportingData.dishId, requestBody, mockMvc)
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(40)
    public void testPatchNonExistingId() throws Exception {
        Map<String, Object> requestBody = Map.of(
                "name", "dish5");
        PatchRequest.performPatchRequest(URL_DISH + UUID.randomUUID(), requestBody, mockMvc)
                .andExpect(status().isNotFound());
    }

    @ParameterizedTest
    @Order(60)
    @MethodSource("tools.values.TestValuesDishes#getGetValidRequests")
    public void testGetAfterPatch(Map<String, Object> requestResponse) throws Exception {
        GetRequestEquals.performGetAndExpect(URL_DISH + SupportingData.dishId, requestResponse, mockMvc);
    }

    @Test
    @Order(99)
    public void testDeleteTemporaryAndAssociatedDataTenant() throws Exception {
        mockMvc.perform(delete(URL_ASSOCIATED + restaurantId)); // delete associated data for test
        mockMvc.perform(delete(URL_TENANT + tenantId + "?forceDelete=true"))
                .andExpect(status().isNoContent());
    }

    @ParameterizedTest
    @Order(100)
    @MethodSource("tools.values.TestValuesDishes#getNonExistingId")
    public void testGetNonExistAndDeleted(String uuid) throws Exception {
        mockMvc.perform(delete(URL_DISH + uuid))
                .andExpect(status().isNotFound());
    }
}
