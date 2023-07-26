import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import ua.com.obox.OboxApplication;
import ua.com.obox.dbschema.tenant.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = OboxApplication.class)
@AutoConfigureMockMvc
@Transactional
public class TenantControllerTest {
    private final String URL = "http://localhost:8080/tenants/";

    @Autowired
    private MockMvc mockMvc;

    @Mock
    private TenantRepository tenantRepository;

    @Before
    public void setup() {
//        Tenant tenant1 = new Tenant("a95de739-40fa-414a-9f62-fdaedb2a8282", "Tenant 1", "ENABLE");
//        Tenant tenant2 = new Tenant("b73c1e7b-72d3-4ab9-9811-9e9aa2f96e71", "Tenant 2", "DISABLE");
//        tenantRepository.saveAll(Arrays.asList(tenant1, tenant2));
    }

    @Test
    public void testGetTenantById_ExistingTenant() throws Exception {
        String tenantTestIdExisting = "0696ac8a-78a5-4239-b455-352e36c700a6";
        Tenant testTenant = new Tenant();
        testTenant.setTenantId(tenantTestIdExisting);
        tenantRepository.save(testTenant);
        mockMvc.perform(get(URL + tenantTestIdExisting))
                .andExpect(status().isOk());
    }

    @Test
    public void testGetTenantById_NotFound() throws Exception {
        mockMvc.perform(get(URL + "non_existent_id"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testCreateWithEmptyName() throws Exception {
        String emptyName = "";
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("name", emptyName);

        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonObject.toString()))
                .andExpect(status().isBadRequest());
    }
}