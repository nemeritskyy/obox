package ua.com.obox.dbschema.restaurant;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ua.com.obox.dbschema.tools.Validator;
import ua.com.obox.dbschema.tools.logging.LoggingService;


@RestController
@RequestMapping("/restaurant")
@RequiredArgsConstructor
public class RestaurantController {
    private final RestaurantService service;
    private final LoggingService loggingService;

    @GetMapping("/{restaurantId}")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "404", description = "Not found")
    })
    public ResponseEntity<RestaurantResponse> getRestaurantById(@PathVariable String restaurantId) {
        RestaurantResponse restaurantResponse = service.getRestaurantById(restaurantId);
        return ResponseEntity.ok(restaurantResponse);
    }

    @PostMapping("/")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Success", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "400", description = "Bad Request")
    })
    public ResponseEntity<RestaurantResponseId> createRestaurant(@RequestBody Restaurant request) {
        Validator.validateName("createRestaurant", request.getName(), loggingService);
        RestaurantResponseId response = service.createRestaurant(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
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
