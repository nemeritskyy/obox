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
                            """
                                    [
                                        {
                                            "menu_id": "05b303aa-8a26-4f80-9c7c-42d13ecc6348",
                                            "restaurant_id": "9aff3e00-451c-490e-b48b-c4315785b75e",
                                            "name": "Non alcohol bar",
                                            "language_code": "en"
                                        },
                                        {
                                            "menu_id": "0c19ee0e-ed72-4d74-a31e-56a366be9b2b",
                                            "restaurant_id": "9aff3e00-451c-490e-b48b-c4315785b75e",
                                            "name": "Hot",
                                            "language_code": "en"
                                        }
                                    ]"""

                    ))),
            @ApiResponse(responseCode = "404", description = "Not found", content = @Content(mediaType = "application/json",
                    schema = @Schema(example =
                            """
                                    {
                                        "timestamp": "2023-08-24T11:48:34.263+00:00",
                                        "status": 404,
                                        "error": "Not Found",
                                        "message": "Menus with Restaurant id 9aff3e00-451c-4x0e-b48b-c4315785b75e not found",
                                        "path": "/restaurants/9aff3e00-451c-4x0e-b48b-c4315785b75e/menus"
                                    }"""

                    )))
    })
    public ResponseEntity<List<MenuResponse>> getAllMenusByRestaurantId(@PathVariable String restaurantId, @RequestHeader HttpHeaders httpHeaders) {
        String acceptLanguage = httpHeaders.getFirst("Accept-Language");
        List<MenuResponse> menuResponses = service.getAllMenusByRestaurantId(restaurantId, acceptLanguage);
        return ResponseEntity.ok(menuResponses);
    }

    @GetMapping("/{restaurantId}")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success", content = @Content(mediaType = "application/json",
                    schema = @Schema(example =
                            """
                                    {
                                        "restaurant_id": "9aff3e00-451c-490e-b48b-c4315785b75e",
                                        "tenant_id": "758a4537-201f-4fdd-a4e0-eefb75c35c60",
                                        "address": "Bohdana st. 32",
                                        "name": "Summer dream"
                                    }"""

                    ))),
            @ApiResponse(responseCode = "404", description = "Not found", content = @Content(mediaType = "application/json",
                    schema = @Schema(example =
                            """
                                    {
                                        "timestamp": "2023-08-24T11:53:36.794+00:00",
                                        "status": 404,
                                        "error": "Not Found",
                                        "message": "Restaurant with id 9aff3e00-451c-49xe-b48b-c4315785b75e not found",
                                        "path": "/restaurants/9aff3e00-451c-49xe-b48b-c4315785b75e"
                                    }"""

                    )))
    })
    public ResponseEntity<RestaurantResponse> getRestaurantById(@PathVariable String restaurantId, @RequestHeader HttpHeaders httpHeaders) {
        String acceptLanguage = httpHeaders.getFirst("Accept-Language");
        RestaurantResponse restaurantResponse = service.getRestaurantById(restaurantId, acceptLanguage);
        return ResponseEntity.ok(restaurantResponse);
    }

    @PostMapping("/")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Success", content = @Content(mediaType = "application/json",
                    schema = @Schema(example =
                            """
                                    {
                                        "menu_id": "0c19ee0e-ed72-4d74-a31e-56a366be9b2b"
                                    }"""
                    ))),
            @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(mediaType = "application/json",
                    schema = @Schema(example =
                            """
                                    {
                                        "timestamp": "2023-08-24T11:58:43.121+00:00",
                                        "status": 400,
                                        "error": "Bad Request",
                                        "message": "400 BAD_REQUEST",
                                        "path": "/menus/",
                                        "fields": {
                                             "tenant_id": "Tenant with id null not found",
                                             "name": "Field name is required"
                                        }
                                    }"""
                    )))
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
                    schema = @Schema(example =
                            """
                                    {
                                        "timestamp": "2023-08-24T11:57:04.949+00:00",
                                        "status": 400,
                                        "error": "Bad Request",
                                        "message": "400 BAD_REQUEST",
                                        "path": "/restaurants/9aff3e00-451c-490e-b48b-c4315785b75e",
                                        "fields": {
                                            "name": "Field name is required"
                                        }
                                    }"""
                    ))),
            @ApiResponse(responseCode = "404", description = "Not found", content = @Content(mediaType = "application/json",
                    schema = @Schema(example =
                            """
                                    {
                                        "timestamp": "2023-08-24T11:56:28.837+00:00",
                                        "status": 404,
                                        "error": "Not Found",
                                        "message": "Restaurant with id 9aff3e00-451c-4x0e-b48b-c4315785b75e not found",
                                        "path": "/restaurants/9aff3e00-451c-4x0e-b48b-c4315785b75e"
                                    }"""
                    )))
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
                    schema = @Schema(example =
                            """
                                    {
                                        "timestamp": "2023-08-24T11:55:25.601+00:00",
                                        "status": 404,
                                        "error": "Not Found",
                                        "message": "Restaurant with id 7fb85bfd-1b63-48bc-bebd-d7d91a352b80 not found",
                                        "path": "/restaurants/7fb85bfd-1b63-48bc-bebd-d7d91a352b80"
                                    }"""
                    )))
    })
    public ResponseEntity<Void> deleteRestaurantById(@PathVariable String restaurantId, @RequestHeader HttpHeaders httpHeaders) {
        String acceptLanguage = httpHeaders.getFirst("Accept-Language");
        service.deleteRestaurantById(restaurantId, acceptLanguage);
        return ResponseEntity.noContent().build();
    }
}
