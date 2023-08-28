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

import java.util.List;


@RestController
@RequestMapping("/restaurants")
@RequiredArgsConstructor

@Tag(name = "Restaurants")
public class RestaurantController {
    private final RestaurantService service;

    @GetMapping("/{restaurantId}/menus")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success", content = @Content(mediaType = "application/json",
                    schema = @Schema(example =
                            "[\n" +
                                    "    {\n" +
                                    "        \"menu_id\": \"05b303aa-8a26-4f80-9c7c-42d13ecc6348\",\n" +
                                    "        \"restaurant_id\": \"9aff3e00-451c-490e-b48b-c4315785b75e\",\n" +
                                    "        \"name\": \"Non alcohol bar\",\n" +
                                    "        \"language_code\": \"en\"\n" +
                                    "    },\n" +
                                    "    {\n" +
                                    "        \"menu_id\": \"0c19ee0e-ed72-4d74-a31e-56a366be9b2b\",\n" +
                                    "        \"restaurant_id\": \"9aff3e00-451c-490e-b48b-c4315785b75e\",\n" +
                                    "        \"name\": \"Hot\",\n" +
                                    "        \"language_code\": \"en\"\n" +
                                    "    }\n" +
                                    "]"

                    ))),
            @ApiResponse(responseCode = "404", description = "Not found", content = @Content(mediaType = "application/json",
                    schema = @Schema(example =
                            "{\n" +
                                    "    \"timestamp\": \"2023-08-24T11:48:34.263+00:00\",\n" +
                                    "    \"status\": 404,\n" +
                                    "    \"error\": \"Not Found\",\n" +
                                    "    \"message\": \"Menus with Restaurant id 9aff3e00-451c-4x0e-b48b-c4315785b75e not found\",\n" +
                                    "    \"path\": \"/restaurants/9aff3e00-451c-4x0e-b48b-c4315785b75e/menus\"\n" +
                                    "}"

                    )))
    })
    public ResponseEntity<List<MenuResponse>> getAllMenusByTenantId(@PathVariable String restaurantId) {
        List<MenuResponse> menuResponses = service.getAllMenusByRestaurantId(restaurantId);
        return ResponseEntity.ok(menuResponses);
    }

    @GetMapping("/{restaurantId}")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success", content = @Content(mediaType = "application/json",
                    schema = @Schema(example =
                            "{\n" +
                                    "    \"restaurant_id\": \"9aff3e00-451c-490e-b48b-c4315785b75e\",\n" +
                                    "    \"tenant_id\": \"758a4537-201f-4fdd-a4e0-eefb75c35c60\",\n" +
                                    "    \"address\": \"Bohdana st. 32\",\n" +
                                    "    \"name\": \"Summer dream\"\n" +
                                    "}"

                    ))),
            @ApiResponse(responseCode = "404", description = "Not found", content = @Content(mediaType = "application/json",
                    schema = @Schema(example =
                            "{\n" +
                                    "    \"timestamp\": \"2023-08-24T11:53:36.794+00:00\",\n" +
                                    "    \"status\": 404,\n" +
                                    "    \"error\": \"Not Found\",\n" +
                                    "    \"message\": \"Restaurant with id 9aff3e00-451c-49xe-b48b-c4315785b75e not found\",\n" +
                                    "    \"path\": \"/restaurants/9aff3e00-451c-49xe-b48b-c4315785b75e\"\n" +
                                    "}"

                    )))
    })
    public ResponseEntity<RestaurantResponse> getRestaurantById(@PathVariable String restaurantId) {
        RestaurantResponse restaurantResponse = service.getRestaurantById(restaurantId);
        return ResponseEntity.ok(restaurantResponse);
    }

    @PostMapping("/")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Success", content = @Content(mediaType = "application/json",
                    schema = @Schema(example =
                            "{\n" +
                                    "    \"menu_id\": \"0c19ee0e-ed72-4d74-a31e-56a366be9b2b\"\n" +
                                    "}"
                    ))),
            @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(mediaType = "application/json",
                    schema = @Schema(example =
                            "{\n" +
                                    "    \"timestamp\": \"2023-08-24T11:58:43.121+00:00\",\n" +
                                    "    \"status\": 400,\n" +
                                    "    \"error\": \"Bad Request\",\n" +
                                    "    \"message\": \"400 BAD_REQUEST\",\n" +
                                    "    \"path\": \"/menus/\",\n" +
                                    "    \"fields\": {\n" +
                                    "        \"language_code\": \"Bad language code must contain from 2 to 3 characters\",\n" +
                                    "        \"restaurant_id\": \"Restaurant with id null not found\",\n" +
                                    "        \"name\": \"Field name is required\"\n" +
                                    "    }\n" +
                                    "}"
                    )))
    })
    public ResponseEntity<RestaurantResponseId> createRestaurant(@RequestBody Restaurant request) {
        RestaurantResponseId response = service.createRestaurant(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("{restaurantId}")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "No Content"),
            @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(mediaType = "application/json",
                    schema = @Schema(example =
                            "{\n" +
                                    "    \"timestamp\": \"2023-08-24T11:57:04.949+00:00\",\n" +
                                    "    \"status\": 400,\n" +
                                    "    \"error\": \"Bad Request\",\n" +
                                    "    \"message\": \"400 BAD_REQUEST\",\n" +
                                    "    \"path\": \"/restaurants/9aff3e00-451c-490e-b48b-c4315785b75e\",\n" +
                                    "    \"fields\": {\n" +
                                    "        \"name\": \"Field name is required\"\n" +
                                    "    }\n" +
                                    "}"
                    ))),
            @ApiResponse(responseCode = "404", description = "Not found", content = @Content(mediaType = "application/json",
                    schema = @Schema(example =
                            "{\n" +
                                    "    \"timestamp\": \"2023-08-24T11:56:28.837+00:00\",\n" +
                                    "    \"status\": 404,\n" +
                                    "    \"error\": \"Not Found\",\n" +
                                    "    \"message\": \"Restaurant with id 9aff3e00-451c-4x0e-b48b-c4315785b75e not found\",\n" +
                                    "    \"path\": \"/restaurants/9aff3e00-451c-4x0e-b48b-c4315785b75e\"\n" +
                                    "}"
                    )))
    })
    public ResponseEntity<Void> patchRestaurantById(@PathVariable String restaurantId, @RequestBody
    @Schema(example = "{\n" +
            "  \"name\": \"string\",\n" +
            "  \"address\": \"string\"" +
            "}")
    Restaurant request) {
        service.patchRestaurantById(restaurantId, request);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{restaurantId}")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "No Content"),
            @ApiResponse(responseCode = "404", description = "Not found", content = @Content(mediaType = "application/json",
                    schema = @Schema(example =
                            "{\n" +
                                    "    \"timestamp\": \"2023-08-24T11:55:25.601+00:00\",\n" +
                                    "    \"status\": 404,\n" +
                                    "    \"error\": \"Not Found\",\n" +
                                    "    \"message\": \"Restaurant with id 7fb85bfd-1b63-48bc-bebd-d7d91a352b80 not found\",\n" +
                                    "    \"path\": \"/restaurants/7fb85bfd-1b63-48bc-bebd-d7d91a352b80\"\n" +
                                    "}"
                    )))
    })
    public ResponseEntity<Void> deleteRestaurantById(@PathVariable String restaurantId) {
        service.deleteRestaurantById(restaurantId);
        return ResponseEntity.noContent().build();
    }
}
