import org.json.JSONObject;
import org.junit.Test;
import org.junit.jupiter.api.BeforeAll;
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
import ua.com.obox.dbschema.tools.State;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = OboxApplication.class)
@AutoConfigureMockMvc
@Transactional
public class TenantControllerTest {
    private final String URL = "http://localhost:8080/tenants/";
    private static final String tenantTestIdExisting = "0696ac8a-78a5-4239-b455-352e36c700a6";
    @Autowired
    private MockMvc mockMvc;
    @Mock
    private TenantRepository tenantRepository;
    private static Tenant testTenant;

    @BeforeAll
    public static void setupTestTenant(@Autowired TenantRepository tenantRepository) {
        testTenant = new Tenant();
        testTenant.setTenantId(tenantTestIdExisting);
        testTenant.setState(State.ENABLED);
        tenantRepository.save(testTenant);
    }

    //    @Before
//    public void setup() {
////        Tenant tenant1 = new Tenant("a95de739-40fa-414a-9f62-fdaedb2a8282", "Tenant 1", "ENABLE");
////        Tenant tenant2 = new Tenant("b73c1e7b-72d3-4ab9-9811-9e9aa2f96e71", "Tenant 2", "DISABLE");
////        tenantRepository.saveAll(Arrays.asList(tenant1, tenant2));
//    }
    @BeforeEach
    public void setup(WebApplicationContext wac) {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
    }

    @Test
    public void testGetTenantById_Existing() throws Exception {
        mockMvc.perform(get(URL + tenantTestIdExisting))
                .andExpect(status().isOk());
    }

    @Test
    public void testGetTenantById_NotFound() throws Exception {
        mockMvc.perform(get(URL + "non_existent_id"))
                .andExpect(status().isNotFound());
    }

    //Bad Request
    @ParameterizedTest
    @MethodSource("getInvalidNames")
    public void testCreateAndPatchNameEmptySpace201Chars(String name) throws Exception {
        assertAll(
                () -> testInvalidNameInRequest(name, post(URL)),
                () -> testInvalidNameInRequest(name, patch(URL + tenantTestIdExisting))
        );
    }

    private void testInvalidNameInRequest(String name, MockHttpServletRequestBuilder requestBuilder) throws Exception {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("name", name);

        mockMvc.perform(requestBuilder
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonObject.toString()))
                .andExpect(status().isBadRequest());
    }

    private static Stream<String> getInvalidNames() {
        return Stream.of("", "    ", "clakxliwqxrunucparyjausiocrpzffjjtnxnvdhxgvtxnzkuzdpxwekxzhvfqcuqgwclllijphouweqkowsuaekfvqxiqfqbyfywdoudlsfhbtqdjizasylsonzzfbnzohqpagbwkotkmciynsukwkdzzfaogilcpyanfyjrzptzjojbqdszzavgobvlwsuuakaoroxc");
    }


//    Code Example
//    @ParameterizedTest
//    @ValueSource(strings = {"", "    ", "clakxliwqxrunucparyjausiocrpzffjjtnxnvdhxgvtxnzkuzdpxwekxzhvfqcuqgwclllijphouweqkowsuaekfvqxiqfqbyfywdoudlsfhbtqdjizasylsonzzfbnzohqpagbwkotkmciynsukwkdzzfaogilcpyanfyjrzptzjojbqdszzavgobvlwsuuakaoroxc"})

//    public void testCreateWithVarcharName() throws Exception { // 201 symbol
//        String name201Chars = "clakxliwqxrunucparyjausiocrpzffjjtnxnvdhxgvtxnzkuzdpxwekxzhvfqcuqgwclllijphouweqkowsuaekfvqxiqfqbyfywdoudlsfhbtqdjizasylsonzzfbnzohqpagbwkotkmciynsukwkdzzfaogilcpyanfyjrzptzjojbqdszzavgobvlwsuuakaoroxc";
//        JSONObject jsonObject = new JSONObject();
//        jsonObject.put("name", name201Chars);
//
//        mockMvc.perform(post(URL)
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(jsonObject.toString()))
//                .andExpect(status().isBadRequest());
//    }
}