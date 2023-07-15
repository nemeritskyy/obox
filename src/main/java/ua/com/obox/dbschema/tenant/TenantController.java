package ua.com.obox.dbschema.tenant;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.NoSuchElementException;

@RestController
@RequestMapping("/tenants")
@RequiredArgsConstructor
public class TenantController {
    private final TenantService service;

    @GetMapping("/{tenantId}")
    public ResponseEntity<TenantResponse> getTenantInfo(@PathVariable String tenantId) {
        try {
            TenantResponse tenantResponse = service.getTenantById(tenantId);
            return ResponseEntity.ok(tenantResponse);
        } catch (NoSuchElementException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Tenant with id " + tenantId + " not found", null);
        }
    }

//    @GetMapping("/name_{tenantName}")
//    public ResponseEntity<TenantResponse> getTenantInfo(@PathVariable String tenantName) {
//        return ResponseEntity.ok(service.getTenantInfo(tenantName));
//    }
}
