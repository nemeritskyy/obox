package ua.com.obox.dbschema.tenant;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import ua.com.obox.dbschema.restaurant.RestaurantResponse;
import ua.com.obox.dbschema.tools.Validator;

import java.util.List;
import java.util.NoSuchElementException;


@RestController
@RequestMapping("/tenants")
@RequiredArgsConstructor

@Tag(name = "Tenants")
public class TenantController {
    private final TenantService service;

    @GetMapping("/{tenantId}/restaurants")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "404", description = "Not found")
    })
    public ResponseEntity<List<RestaurantResponse>> getAllRestaurantsByTenantId(@PathVariable String tenantId) {
        List<RestaurantResponse> restaurantResponses = service.getAllRestaurantsByTenantId(tenantId);
        return ResponseEntity.ok(restaurantResponses);
    }

    @GetMapping("/{tenantId}")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "404", description = "Not found")
    })
    public ResponseEntity<TenantResponse> getTenantById(@PathVariable String tenantId) {
        try {
            TenantResponse tenantResponse = service.getTenantById(tenantId);
            return ResponseEntity.ok(tenantResponse);
        } catch (NoSuchElementException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Tenant with id " + tenantId + " not found", null);
        }
    }

    @PostMapping("/")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Success", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "400", description = "Bad Request")
    })
    public ResponseEntity<TenantResponseId> createTenant(@RequestBody Tenant request) {
        Validator.validateName(request.getName());
        TenantResponseId response = service.createTenant(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("{tenantId}")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "No Content"),
            @ApiResponse(responseCode = "404", description = "Not found")
    })
    public ResponseEntity<Void> patchTenantById(@PathVariable String tenantId, @RequestBody Tenant request) {
        try {
            Validator.validateName(request.getName());
            service.patchTenantById(tenantId, request);
            return ResponseEntity.noContent().build();
        } catch (NoSuchElementException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Tenant with id " + tenantId + " not found", null);
        }
    }

    @DeleteMapping("/{tenantId}")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "No Content"),
            @ApiResponse(responseCode = "404", description = "Not found")
    })
    public ResponseEntity<TenantResponseId> deleteTenantById(@PathVariable String tenantId) {
        try {
            service.deleteTenantById(tenantId);
            return ResponseEntity.noContent().build();
        } catch (NoSuchElementException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Tenant with id " + tenantId + " not found", null);
        }
    }
}
