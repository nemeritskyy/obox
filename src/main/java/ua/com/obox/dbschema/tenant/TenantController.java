package ua.com.obox.dbschema.tenant;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ua.com.obox.dbschema.restaurant.RestaurantResponse;

import java.util.List;


@RestController
@RequestMapping("/tenants")
@RequiredArgsConstructor

@Tag(name = "Tenants")
public class TenantController {
    private final TenantService service;

    @GetMapping("/{tenantId}/restaurants")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success", content = @Content(mediaType = "application/json",
                    schema = @Schema(example =
                            """
                                    [
                                        {
                                            "restaurant_id": "f65ce9ff-b1b1-4059-ae18-d66d7f43428d",
                                            "tenant_id": "758a4537-201f-4fdd-a4e0-eefb75c35c60",
                                            "address": "Kyiv, st. Tupoleva",
                                            "name": "Ali Street"
                                        },
                                        {
                                            "restaurant_id": "31ed0fa1-61c5-46fb-ae1c-b8ed630c853f",
                                            "tenant_id": "758a4537-201f-4fdd-a4e0-eefb75c35c60",
                                            "name": "Belissimo"
                                        }
                                    ]"""

                    ))),
            @ApiResponse(responseCode = "404", description = "Not found", content = @Content(mediaType = "application/json",
                    schema = @Schema(example =
                            """
                                    {
                                        "timestamp": "2023-08-24T07:11:46.352+00:00",
                                        "status": 404,
                                        "error": "Not Found",
                                        "message": "Restaurants with Tenant id 5924e0aa-7dc6-4889-8x70-cf5f339b12d1 not found",
                                        "path": "/tenants/5924e0aa-7dc6-4889-8x70-cf5f339b12d1/restaurants"
                                    }"""

                    )))
    })
    public ResponseEntity<List<RestaurantResponse>> getAllRestaurantsByTenantId(@PathVariable String tenantId, @RequestHeader HttpHeaders httpHeaders) {
        String acceptLanguage = httpHeaders.getFirst("Accept-Language");
        List<RestaurantResponse> restaurantResponses = service.getAllRestaurantsByTenantId(tenantId, acceptLanguage);
        return ResponseEntity.ok(restaurantResponses);
    }

    @GetMapping("/{tenantId}")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success", content = @Content(mediaType = "application/json",
                    schema = @Schema(example =
                            """
                                    {
                                        "tenant_id": "e8978620-fcd2-458a-9665-3bb4b4b5b66d",
                                        "name": "Syshi Hushiya"
                                    }"""

                    ))),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(mediaType = "application/json",
            schema = @Schema(example =
                    """
                            {
                                "timestamp": "2023-08-24T06:34:35.850+00:00",
                                "status": 403,
                                "error": "Forbidden",
                                "message": "Tenant with id a95de739-40fa-414a-9f62-fdaedb2a8282 forbidden",
                                "path": "/tenants/a95de739-40fa-414a-9f62-fdaedb2a8282"
                            }"""

            ))),
            @ApiResponse(responseCode = "404", description = "Not found", content = @Content(mediaType = "application/json",
                    schema = @Schema(example =
                            """
                                    {
                                        "timestamp": "2023-08-24T06:58:11.712+00:00",
                                        "status": 404,
                                        "error": "Not Found",
                                        "message": "Tenant with id a95de739-40fa-414a-9f62-fxaedb2a8282 not found",
                                        "path": "/tenants/a95de739-40fa-414a-9f62-fxaedb2a8282"
                                    }"""

                    )))
    })
    public ResponseEntity<TenantResponse> getTenantById(@PathVariable String tenantId, @RequestHeader HttpHeaders httpHeaders) {
        String acceptLanguage = httpHeaders.getFirst("Accept-Language");
        TenantResponse tenantResponse = service.getTenantById(tenantId, acceptLanguage);
        return ResponseEntity.ok(tenantResponse);
    }

    @PostMapping("/")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Success", content = @Content(mediaType = "application/json",
                    schema = @Schema(example =
                            """
                                    {
                                        "tenant_id": "c6f22a4c-1d7f-4f3c-ab43-a4986db87e34"
                                    }"""
                    ))),
            @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(mediaType = "application/json",
                    schema = @Schema(example =
                            """
                                    {
                                        "timestamp": "2023-08-24T05:53:13.926+00:00",
                                        "status": 400,
                                        "error": "Bad Request",
                                        "message": "400 BAD_REQUEST",
                                        "path": "/tenants/",
                                        "fields": {
                                            "name": "Field name is required"
                                        }
                                    }"""
                    )))
    })
    public ResponseEntity<TenantResponseId> createTenant(@RequestBody Tenant request, @RequestHeader HttpHeaders httpHeaders) {
        String acceptLanguage = httpHeaders.getFirst("Accept-Language");
        TenantResponseId response = service.createTenant(request, acceptLanguage);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("{tenantId}")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "No Content"),
            @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(mediaType = "application/json",
                    schema = @Schema(example =
                            """
                                    {
                                        "timestamp": "2023-08-24T06:59:51.321+00:00",
                                        "status": 400,
                                        "error": "Bad Request",
                                        "message": "400 BAD_REQUEST",
                                        "path": "/tenants/818059d7-bd65-44f5-b2ab-c4d716541a43",
                                        "fields": {
                                            "name": "Field name is required"
                                        }
                                    }"""
                    ))),
            @ApiResponse(responseCode = "404", description = "Not found", content = @Content(mediaType = "application/json",
                    schema = @Schema(example =
                            """
                                    {
                                        "timestamp": "2023-08-24T07:04:33.042+00:00",
                                        "status": 404,
                                        "error": "Not Found",
                                        "message": "Tenant with id 818059d7-bd65-44f5-b2ab-cxd716541a43 not found",
                                        "path": "/tenants/818059d7-bd65-44f5-b2ab-cxd716541a43"
                                    }"""
                    )))
    })
    public ResponseEntity<Void> patchTenantById(@PathVariable String tenantId, @RequestBody Tenant request, @RequestHeader HttpHeaders httpHeaders) {
        String acceptLanguage = httpHeaders.getFirst("Accept-Language");
        service.patchTenantById(tenantId, request, acceptLanguage);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{tenantId}")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "No Content"),
            @ApiResponse(responseCode = "404", description = "Not found", content = @Content(mediaType = "application/json",
                    schema = @Schema(example =
                            """
                                    {
                                        "timestamp": "2023-08-24T06:40:39.627+00:00",
                                        "status": 404,
                                        "error": "Not Found",
                                        "message": "Tenant with id b2268525-099d-4e8e-80ce-x258066c3aec not found",
                                        "path": "/tenants/b2268525-099d-4e8e-80ce-x258066c3aec"
                                    }"""
                    )))
    })
    public ResponseEntity<Void> deleteTenantById(@PathVariable String tenantId, @RequestParam(required = false) boolean forceDelete, @RequestHeader HttpHeaders httpHeaders) {
        String acceptLanguage = httpHeaders.getFirst("Accept-Language");
        service.deleteTenantById(tenantId, forceDelete, acceptLanguage);
        return ResponseEntity.noContent().build();
    }
}
