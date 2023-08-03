package ua.com.obox.dbschema.restaurant;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ua.com.obox.dbschema.menu.MenuResponse;
import ua.com.obox.dbschema.tools.Validator;
import ua.com.obox.dbschema.tools.logging.LoggingService;

import java.util.List;


@RestController
@RequestMapping("/restaurants")
@RequiredArgsConstructor

@Tag(name = "Restaurants")
public class RestaurantController {
    private final RestaurantService service;
    private final LoggingService loggingService;
    private String loggingMessage;

    @GetMapping("/{restaurantId}/menus")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "404", description = "Not found")
    })
    public ResponseEntity<List<MenuResponse>> getAllMenusByTenantId(@PathVariable String restaurantId) {
        List<MenuResponse> menuResponses = service.getAllMenusByRestaurantId(restaurantId);
        return ResponseEntity.ok(menuResponses);
    }

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
        loggingMessage = "createRestaurant";
        Validator.validateName(loggingMessage, request.getName(), loggingService);
        RestaurantResponseId response = service.createRestaurant(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("{restaurantId}")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "No Content"),
            @ApiResponse(responseCode = "404", description = "Not found")
    })
    public ResponseEntity<Void> patchRestaurantById(@PathVariable String restaurantId, @RequestBody
    @Schema(example = "{\n" +
            "  \"name\": \"string\",\n" +
            "  \"address\": \"string\"" +
            "}")
    Restaurant request) {
        loggingMessage = "patchRestaurantById";
        service.patchRestaurantById(restaurantId, request);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{restaurantId}")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "No Content"),
            @ApiResponse(responseCode = "404", description = "Not found")
    })
    public ResponseEntity<Void> deleteRestaurantById(@PathVariable String restaurantId) {
        service.deleteRestaurantById(restaurantId);
        return ResponseEntity.noContent().build();
    }
}
