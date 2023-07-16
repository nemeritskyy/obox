package ua.com.obox.dbschema.tenant;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/tenants")
@RequiredArgsConstructor
public class TenantController {
    private final TenantService service;

    @GetMapping("/{tenantId}")
    public ResponseEntity<TenantResponse> getTenantById(@PathVariable String tenantId) {
        try {
            TenantResponse tenantResponse = service.getTenantById(tenantId);
            return ResponseEntity.ok(tenantResponse);
        } catch (NoSuchElementException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Tenant with id " + tenantId + " not found", null);
        }
    }

    @PostMapping("/")
    public ResponseEntity<Map<String, String>> createTenant(@RequestBody Tenant request) {
        if (request.getName() == null || request.getName().isEmpty())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Name field is required", null);
        if (request.getName().length() > 200)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Name field must contain from 1 to 200 characters", null);
        Map<String, String> response = service.createTenant(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("{tenantId}")
    public ResponseEntity<Map<String, String>> patchTenantById(@PathVariable String tenantId, @RequestBody Tenant request) {
        try {
            if (request.getName() == null || request.getName().isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Name field is required");
            }
            if (request.getName().length() > 200) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Name field must contain from 1 to 200 characters");
            }
            Map<String, String> response = service.patchTenantById(tenantId, request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (NoSuchElementException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Tenant with id " + tenantId + " not found", null);
        }
    }

    @DeleteMapping("/{tenantId}")
    public ResponseEntity<Map<String, String>> deleteTenantById(@PathVariable String tenantId) {
        try {
            Map<String, String> response = service.deleteTenantById(tenantId);
            return ResponseEntity.ok(response);
        } catch (NoSuchElementException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Tenant with id " + tenantId + " not found", null);
        }
    }
}
