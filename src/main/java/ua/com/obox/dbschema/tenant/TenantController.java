package ua.com.obox.dbschema.tenant;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ua.com.obox.dbschema.restaurant.RestaurantResponse;

import java.util.List;

import static ua.com.obox.dbschema.tools.examples.TenantResponseExample.*;


@RestController
@RequestMapping("/tenants")
@PreAuthorize("hasAnyRole('ADMIN','USER')")
@RequiredArgsConstructor

@Tag(name = "Tenants")
public class TenantController {
    private final TenantService service;

    @GetMapping("/{tenantId}/restaurants")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success", content = @Content(mediaType = "application/json",
                    schema = @Schema(example = GET_ALL_200_RESPONSE_EXAMPLE))),
            @ApiResponse(responseCode = "404", description = "Not found", content = @Content(mediaType = "application/json",
                    schema = @Schema(example = ALL_MAPPINGS_404_RESPONSE_EXAMPLE)))
    })
    public ResponseEntity<List<RestaurantResponse>> getAllRestaurantsByTenantId(@PathVariable String tenantId, @RequestHeader HttpHeaders httpHeaders) {
        String acceptLanguage = httpHeaders.getFirst("Accept-Language");
        List<RestaurantResponse> restaurantResponses = service.getAllRestaurantsByTenantId(tenantId, acceptLanguage);
        return ResponseEntity.ok(restaurantResponses);
    }

    @GetMapping("/{tenantId}")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success", content = @Content(mediaType = "application/json",
                    schema = @Schema(example = GET_200_RESPONSE_EXAMPLE))),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(mediaType = "application/json",
                    schema = @Schema(example = GET_403_RESPONSE_EXAMPLE))),
            @ApiResponse(responseCode = "404", description = "Not found", content = @Content(mediaType = "application/json",
                    schema = @Schema(example = ALL_MAPPINGS_404_RESPONSE_EXAMPLE)))
    })
    public ResponseEntity<TenantResponse> getTenantById(@PathVariable String tenantId, @RequestHeader HttpHeaders httpHeaders) throws JsonProcessingException {
        String acceptLanguage = httpHeaders.getFirst("Accept-Language");
        TenantResponse tenantResponse = service.getTenantById(tenantId, acceptLanguage);
        return ResponseEntity.ok(tenantResponse);
    }

    @PostMapping("/")
    @PreAuthorize("hasRole('ADMIN')")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Success", content = @Content(mediaType = "application/json",
                    schema = @Schema(example = POST_201_RESPONSE_EXAMPLE))),
            @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(mediaType = "application/json",
                    schema = @Schema(example = POST_400_RESPONSE_EXAMPLE)))
    })
    public ResponseEntity<TenantResponseId> createTenant(@RequestBody @Schema(example = POST_BODY) Tenant request, @RequestHeader HttpHeaders httpHeaders) throws JsonProcessingException {
        String acceptLanguage = httpHeaders.getFirst("Accept-Language");
        TenantResponseId response = service.createTenant(request, acceptLanguage);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("{tenantId}")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "No Content"),
            @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(mediaType = "application/json",
                    schema = @Schema(example = PATCH_400_RESPONSE_EXAMPLE))),
            @ApiResponse(responseCode = "404", description = "Not found", content = @Content(mediaType = "application/json",
                    schema = @Schema(example = ALL_MAPPINGS_404_RESPONSE_EXAMPLE)))
    })
    public ResponseEntity<Void> patchTenantById(@PathVariable String tenantId, @RequestBody @Schema(example = POST_BODY) Tenant request, @RequestHeader HttpHeaders httpHeaders) throws JsonProcessingException {
        String acceptLanguage = httpHeaders.getFirst("Accept-Language");
        service.patchTenantById(tenantId, request, acceptLanguage);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{tenantId}")
    @PreAuthorize("hasRole('ADMIN')")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "No Content"),
            @ApiResponse(responseCode = "404", description = "Not found", content = @Content(mediaType = "application/json",
                    schema = @Schema(example = ALL_MAPPINGS_404_RESPONSE_EXAMPLE)))
    })
    public ResponseEntity<Void> deleteTenantById(@PathVariable String tenantId, @RequestParam(required = false) boolean forceDelete, @RequestHeader HttpHeaders httpHeaders) {
        String acceptLanguage = httpHeaders.getFirst("Accept-Language");
        service.deleteTenantById(tenantId, forceDelete, acceptLanguage);
        return ResponseEntity.noContent().build();
    }
}