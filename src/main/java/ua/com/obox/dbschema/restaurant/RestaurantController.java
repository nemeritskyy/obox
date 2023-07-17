package ua.com.obox.dbschema.restaurant;

import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import ua.com.obox.dbschema.tenant.Tenant;
import ua.com.obox.dbschema.tenant.TenantResponse;
import ua.com.obox.dbschema.tenant.TenantResponseId;

import java.util.NoSuchElementException;

@Hidden
@RestController
@RequestMapping("/restaurant")
@RequiredArgsConstructor
public class RestaurantController {
    private final RestaurantService service;

    @GetMapping("/{restaurantId}")
    public ResponseEntity<RestaurantResponse> getRestaurantById(@PathVariable String restaurantId) {
        try {
            RestaurantResponse restaurantResponse = service.getRestaurantById(restaurantId);
            return ResponseEntity.ok(restaurantResponse);
        } catch (NoSuchElementException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Restaurant with id " + restaurantId + " not found", null);
        }
    }

//    @PostMapping("/")
//    public ResponseEntity<TenantResponseId> createTenant(@RequestBody Tenant request) {
//        if (request.getName() == null || request.getName().isEmpty())
//            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Name field is required", null);
//        if (request.getName().length() > 200)
//            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Name field must contain from 1 to 200 characters", null);
//        TenantResponseId response = service.createTenant(request);
//        return ResponseEntity.status(HttpStatus.CREATED).body(response);
//    }
//
//    @PatchMapping("{tenantId}")
//    public ResponseEntity<Void> patchTenantById(@PathVariable String tenantId, @RequestBody Tenant request) {
//        try {
//            if (request.getName() == null || request.getName().isEmpty()) {
//                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Name field is required");
//            }
//            if (request.getName().length() > 200) {
//                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Name field must contain from 1 to 200 characters");
//            }
//            service.patchTenantById(tenantId, request);
//            return ResponseEntity.noContent().build();
//        } catch (NoSuchElementException e) {
//            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Tenant with id " + tenantId + " not found", null);
//        }
//    }
//
//    @DeleteMapping("/{tenantId}")
//    public ResponseEntity<TenantResponseId> deleteTenantById(@PathVariable String tenantId) {
//        try {
//            service.deleteTenantById(tenantId);
//            return ResponseEntity.noContent().build();
//        } catch (NoSuchElementException e) {
//            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Tenant with id " + tenantId + " not found", null);
//        }
//    }
}
