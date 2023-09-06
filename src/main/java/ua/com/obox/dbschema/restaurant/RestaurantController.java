package ua.com.obox.dbschema.restaurant;

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
import ua.com.obox.dbschema.menu.MenuResponse;

import java.util.List;

import static ua.com.obox.dbschema.tools.examples.RestaurantResponseExample.*;


@RestController
@RequestMapping("/restaurants")
@RequiredArgsConstructor

@Tag(name = "Restaurants")
public class RestaurantController {
    private final RestaurantService service;

    @GetMapping("/{restaurantId}/menus")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success", content = @Content(mediaType = "application/json",
                    schema = @Schema(example = GET_ALL_200_RESPONSE_EXAMPLE))),
            @ApiResponse(responseCode = "404", description = "Not found", content = @Content(mediaType = "application/json",
                    schema = @Schema(example = ALL_MAPPINGS_404_RESPONSE_EXAMPLE)))
    })
    public ResponseEntity<List<MenuResponse>> getAllMenusByRestaurantId(@PathVariable String restaurantId, @RequestHeader HttpHeaders httpHeaders) {
        String acceptLanguage = httpHeaders.getFirst("Accept-Language");
        List<MenuResponse> menuResponses = service.getAllMenusByRestaurantId(restaurantId, acceptLanguage);
        return ResponseEntity.ok(menuResponses);
    }

    @GetMapping("/{restaurantId}")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success", content = @Content(mediaType = "application/json",
                    schema = @Schema(example = GET_200_RESPONSE_EXAMPLE))),
            @ApiResponse(responseCode = "404", description = "Not found", content = @Content(mediaType = "application/json",
                    schema = @Schema(example = ALL_MAPPINGS_404_RESPONSE_EXAMPLE)))
    })
    public ResponseEntity<RestaurantResponse> getRestaurantById(@PathVariable String restaurantId, @RequestHeader HttpHeaders httpHeaders) {
        String acceptLanguage = httpHeaders.getFirst("Accept-Language");
        RestaurantResponse restaurantResponse = service.getRestaurantById(restaurantId, acceptLanguage);
        return ResponseEntity.ok(restaurantResponse);
    }

    @PostMapping("/")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Success", content = @Content(mediaType = "application/json",
                    schema = @Schema(example = POST_201_RESPONSE_EXAMPLE))),
            @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(mediaType = "application/json",
                    schema = @Schema(example = POST_400_RESPONSE_EXAMPLE)))
    })
    public ResponseEntity<RestaurantResponseId> createRestaurant(@RequestBody Restaurant request, @RequestHeader HttpHeaders httpHeaders) {
        String acceptLanguage = httpHeaders.getFirst("Accept-Language");
        RestaurantResponseId response = service.createRestaurant(request, acceptLanguage);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("{restaurantId}")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "No Content"),
            @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(mediaType = "application/json",
                    schema = @Schema(example = PATCH_400_RESPONSE_EXAMPLE))),
            @ApiResponse(responseCode = "404", description = "Not found", content = @Content(mediaType = "application/json",
                    schema = @Schema(example = ALL_MAPPINGS_404_RESPONSE_EXAMPLE)))
    })
    public ResponseEntity<Void> patchRestaurantById(@PathVariable String restaurantId, @RequestBody
    @Schema(example = """
            {
              "name": "string",
              "address": "string"}""")
    Restaurant request, @RequestHeader HttpHeaders httpHeaders) {
        String acceptLanguage = httpHeaders.getFirst("Accept-Language");
        service.patchRestaurantById(restaurantId, request, acceptLanguage);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{restaurantId}")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "No Content"),
            @ApiResponse(responseCode = "404", description = "Not found", content = @Content(mediaType = "application/json",
                    schema = @Schema(example = ALL_MAPPINGS_404_RESPONSE_EXAMPLE)))
    })
    public ResponseEntity<Void> deleteRestaurantById(@PathVariable String restaurantId, @RequestHeader HttpHeaders httpHeaders) {
        String acceptLanguage = httpHeaders.getFirst("Accept-Language");
        service.deleteRestaurantById(restaurantId, acceptLanguage);
        return ResponseEntity.noContent().build();
    }
}