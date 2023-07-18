package ua.com.obox.dbschema.tenant;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ua.com.obox.dbschema.restaurant.RestaurantResponse;
import ua.com.obox.dbschema.tools.Validator;
import ua.com.obox.dbschema.tools.logging.LoggingService;

import java.util.List;


@RestController
@RequestMapping("/tenants")
@RequiredArgsConstructor

@Tag(name = "Tenants")
public class TenantController {
    private final TenantService service;
    private final LoggingService loggingService;

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
        TenantResponse tenantResponse = service.getTenantById(tenantId);
        return ResponseEntity.ok(tenantResponse);
    }

    @PostMapping("/")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Success", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "400", description = "Bad Request")
    })
    public ResponseEntity<TenantResponseId> createTenant(@RequestBody Tenant request) {
        Validator.validateName("createTenant", request.getName(), loggingService);
        TenantResponseId response = service.createTenant(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("{tenantId}")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "No Content"),
            @ApiResponse(responseCode = "404", description = "Not found")
    })
    public ResponseEntity<Void> patchTenantById(@PathVariable String tenantId, @RequestBody Tenant request) {
        Validator.validateName("patchTenantById", request.getName(), loggingService);
        service.patchTenantById(tenantId, request);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{tenantId}")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "No Content"),
            @ApiResponse(responseCode = "404", description = "Not found")
    })
    public ResponseEntity<Void> deleteTenantById(@PathVariable String tenantId) {
        service.deleteTenantById(tenantId);
        return ResponseEntity.noContent().build();
    }
}
