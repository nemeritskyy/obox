package ua.com.obox.dbschema.tenant;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import ua.com.obox.dbschema.restaurant.RestaurantResponse;

import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/tenants")
@RequiredArgsConstructor
public class TenantController {
    private final TenantService service;

    @GetMapping("/{tenantId}/restaurants")
    public ResponseEntity<List<RestaurantResponse>> getAllRestaurantsByTenantId(@PathVariable String tenantId) {
        try {
            List<RestaurantResponse> restaurantResponses = service.getAllRestaurantsByTenantId(tenantId);
            return ResponseEntity.ok(restaurantResponses);
        } catch (NoSuchElementException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Restaurants with Tenant id " + tenantId + " not found", null);
        }
    }

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
    public ResponseEntity<TenantResponseId> createTenant(@RequestBody Tenant request) {
        if (request.getName() == null || request.getName().isEmpty())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Name field is required", null);
        if (request.getName().length() > 200)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Name field must contain from 1 to 200 characters", null);
        TenantResponseId response = service.createTenant(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("{tenantId}")
    public ResponseEntity<Void> patchTenantById(@PathVariable String tenantId, @RequestBody Tenant request) {
        try {
            if (request.getName() == null || request.getName().isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Name field is required");
            }
            if (request.getName().length() > 200) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Name field must contain from 1 to 200 characters");
            }
            service.patchTenantById(tenantId, request);
            return ResponseEntity.noContent().build();
        } catch (NoSuchElementException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Tenant with id " + tenantId + " not found", null);
        }
    }

    @DeleteMapping("/{tenantId}")
    public ResponseEntity<TenantResponseId> deleteTenantById(@PathVariable String tenantId) {
        try {
            service.deleteTenantById(tenantId);
            return ResponseEntity.noContent().build();
        } catch (NoSuchElementException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Tenant with id " + tenantId + " not found", null);
        }
    }
}
