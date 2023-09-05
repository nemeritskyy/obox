package ua.com.obox.dbschema.menu;

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
import ua.com.obox.dbschema.category.CategoryResponse;

import java.util.List;

@RestController
@RequestMapping("/menus")
@RequiredArgsConstructor
@Tag(name = "Menus")
public class MenuController {

    private final MenuService service;

    @GetMapping("/{menuId}/categories")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success", content = @Content(mediaType = "application/json",
                    schema = @Schema(example =
                            """
                                    [
                                        {
                                            "category_id": "dec66073-063c-4c58-ac97-2aab5b559dd4",
                                            "name": "Fresh",
                                            "menu_id": "05b303aa-8a26-4f80-9c7c-42d13ecc6348"
                                        },
                                        {
                                            "category_id": "fd236b1e-8103-4c06-872c-c796262aa795",
                                            "name": "Vegetables",
                                            "menu_id": "05b303aa-8a26-4f80-9c7c-42d13ecc6348"
                                        }
                                    ]"""

                    ))),
            @ApiResponse(responseCode = "404", description = "Not found", content = @Content(mediaType = "application/json",
                    schema = @Schema(example =
                            """
                                    {
                                        "timestamp": "2023-08-24T12:07:03.855+00:00",
                                        "status": 404,
                                        "error": "Not Found",
                                        "message": "Menu with id 05b303aa-8a26-xf80-9c7c-42d13ecc6348 not found",
                                        "path": "/menus/05b303aa-8a26-xf80-9c7c-42d13ecc6348/categories"
                                    }"""

                    )))
    })
    public ResponseEntity<List<CategoryResponse>> getAllCategoriesByMenuId(@PathVariable String menuId, @RequestHeader HttpHeaders httpHeaders) {
        String acceptLanguage = httpHeaders.getFirst("Accept-Language");
        List<CategoryResponse> categoryResponses = service.getAllCategoriesByMenuId(menuId, acceptLanguage);
        return ResponseEntity.ok(categoryResponses);
    }

    @GetMapping("/{menuId}")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success", content = @Content(mediaType = "application/json",
                    schema = @Schema(example =
                            """
                                    {
                                        "menu_id": "05b303aa-8a26-4f80-9c7c-42d13ecc6348",
                                        "restaurant_id": "9aff3e00-451c-490e-b48b-c4315785b75e",
                                        "name": "Non alcohol bar",
                                        "language_code": "en"
                                    }"""

                    ))),
            @ApiResponse(responseCode = "404", description = "Not found", content = @Content(mediaType = "application/json",
                    schema = @Schema(example =
                            """
                                    {
                                        "timestamp": "2023-08-25T05:10:43.103+00:00",
                                        "status": 404,
                                        "error": "Not Found",
                                        "message": "Menu with id 05b303aa-8a26-4fx0-9c7c-42d13ecc6348 not found",
                                        "path": "/menus/05b303aa-8a26-4fx0-9c7c-42d13ecc6348"
                                    }"""

                    )))
    })
    public ResponseEntity<MenuResponse> getMenuById(@PathVariable String menuId, @RequestHeader HttpHeaders httpHeaders) {
        String acceptLanguage = httpHeaders.getFirst("Accept-Language");
        MenuResponse menuResponse = service.getMenuById(menuId, acceptLanguage);
        return ResponseEntity.ok(menuResponse);
    }

    @PostMapping("/")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Success", content = @Content(mediaType = "application/json",
                    schema = @Schema(example =
                            """
                                    {
                                        "menu_id": "b4d331ec-ec54-4789-b8f3-1b3435eaa52a"
                                    }"""
                    ))),
            @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(mediaType = "application/json",
                    schema = @Schema(example =
                            """
                                    {
                                        "timestamp": "2023-08-25T05:11:14.711+00:00",
                                        "status": 400,
                                        "error": "Bad Request",
                                        "message": "400 BAD_REQUEST",
                                        "path": "/menus/",
                                        "fields": {
                                            "language_code": "Bad language code must contain from 2 to 3 characters",
                                            "restaurant_id": "Restaurant with id null not found",
                                            "name": "Field name is required"
                                        }
                                    }"""
                    )))
    })
    public ResponseEntity<MenuResponseId> createMenu(@RequestBody Menu request, @RequestHeader HttpHeaders httpHeaders) {
        String acceptLanguage = httpHeaders.getFirst("Accept-Language");
        MenuResponseId response = service.createMenu(request, acceptLanguage);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("{menuId}")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "No Content"),
            @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(mediaType = "application/json",
                    schema = @Schema(example =
                            """
                                    {
                                        "timestamp": "2023-08-25T05:15:40.318+00:00",
                                        "status": 400,
                                        "error": "Bad Request",
                                        "message": "400 BAD_REQUEST",
                                        "path": "/menus/b4d331ec-ec54-4789-b8f3-1b3435eaa52a",
                                        "fields": {
                                            "name": "Field name is required"
                                        }
                                    }"""
                    ))),
            @ApiResponse(responseCode = "404", description = "Not found", content = @Content(mediaType = "application/json",
                    schema = @Schema(example =
                            """
                                    {
                                        "timestamp": "2023-08-25T05:15:14.520+00:00",
                                        "status": 404,
                                        "error": "Not Found",
                                        "message": "Menu with id b4d331ec-ec54-47x9-b8f3-1b3435eaa52a not found",
                                        "path": "/menus/b4d331ec-ec54-47x9-b8f3-1b3435eaa52a"
                                    }"""
                    )))
    })
    public ResponseEntity<Void> patchMenuById(@PathVariable String menuId, @RequestBody
    @Schema(example = "{\n" +
            "  \"name\": \"string\"" +
            "}")
    Menu request, @RequestHeader HttpHeaders httpHeaders) {
        String acceptLanguage = httpHeaders.getFirst("Accept-Language");
        service.patchMenuById(menuId, request, acceptLanguage);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{menuId}")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "No Content"),
            @ApiResponse(responseCode = "404", description = "Not found", content = @Content(mediaType = "application/json",
                    schema = @Schema(example =
                            """
                                    {
                                        "timestamp": "2023-08-25T05:16:10.781+00:00",
                                        "status": 404,
                                        "error": "Not Found",
                                        "message": "Menu with id 3385cf94-d26e-4c72-acx6-19b2a5e19982 not found",
                                        "path": "/menus/3385cf94-d26e-4c72-acx6-19b2a5e19982"
                                    }"""
                    )))
    })
    public ResponseEntity<Void> deleteMenuById(@PathVariable String menuId, @RequestHeader HttpHeaders httpHeaders) {
        String acceptLanguage = httpHeaders.getFirst("Accept-Language");
        service.deleteMenuById(menuId, acceptLanguage);
        return ResponseEntity.noContent().build();
    }
}
