package ua.com.obox.dbschema.category;

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
import ua.com.obox.dbschema.dish.DishResponse;

import java.util.List;

@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
@Tag(name = "Categories")
public class CategoryController {
    private final CategoryService service;

    @GetMapping("/{categoryId}/dishes")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success", content = @Content(mediaType = "application/json",
                    schema = @Schema(example =
                            """
                                    [
                                        {
                                            "dish_id": "260c74e4-c6d2-46bd-9b5f-e6c3e4be9cc7",
                                            "category_id": "6a1ad72d-f085-4e6e-bf17-ff6108de2321",
                                            "associated_id": "26e8fa15-2245-4d50-b4b5-b23eb1eb8bd2",
                                            "name": "Fish Le Fish",
                                            "description": null,
                                            "price": 100.0,
                                            "weight": 10,
                                            "calories": 300,
                                            "allergens": [
                                               "Fish",
                                               "Soy"
                                            ],
                                            "tags": [
                                               "Vegeteriab"
                                            ],        "image_url": "https://img.obox.com.ua/26e8fa15-2245-4d50-b4b5-b23eb1eb8bd2/260c74e4-c6d2-46bd-9b5f-e6c3e4be9cc7.jpg",
                                            "state": "ENABLED"
                                        },
                                        {
                                            "dish_id": "a898e1db-92e9-408e-bae0-9c229b965f0a",
                                            "category_id": "6a1ad72d-f085-4e6e-bf17-ff6108de2321",
                                            "associated_id": "26e8fa15-2245-4d50-b4b5-b23eb1eb8bd2",
                                            "name": "Fish O Fish",
                                            "description": null,
                                            "price": 100.0,
                                            "weight": 10,
                                            "calories": 300,
                                            "allergens": [
                                                "Fish",
                                                "Soy"
                                            ],
                                            "tags": [],        "image_url": "https://img.obox.com.ua/26e8fa15-2245-4d50-b4b5-b23eb1eb8bd2/a898e1db-92e9-408e-bae0-9c229b965f0a.jpg",
                                            "state": "ENABLED"
                                        }
                                    ]"""

                    ))),
            @ApiResponse(responseCode = "404", description = "Not found", content = @Content(mediaType = "application/json",
                    schema = @Schema(example =
                            """
                                    {
                                        "timestamp": "2023-08-25T05:35:51.544+00:00",
                                        "status": 404,
                                        "error": "Not Found",
                                        "message": "Dishes with Category id 6a1ad72d-f0x5-4e6e-bf17-ff6108de2321 not found",
                                        "path": "/categories/6a1ad72d-f0x5-4e6e-bf17-ff6108de2321/dishes"
                                    }"""

                    )))
    })
    public ResponseEntity<List<DishResponse>> getAllDishesByCategoryId(@PathVariable String categoryId, @RequestHeader HttpHeaders httpHeaders) {
        String acceptLanguage = httpHeaders.getFirst("Accept-Language");
        List<DishResponse> categoryResponses = service.getAllDishesByCategoryId(categoryId, acceptLanguage);
        return ResponseEntity.ok(categoryResponses);
    }

    @GetMapping("/{categoryId}")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success", content = @Content(mediaType = "application/json",
                    schema = @Schema(example =
                            """
                                    {
                                        "category_id": "fd236b1e-8103-4c06-872c-c796262aa795",
                                        "name": "Vegetables",
                                        "menu_id": "05b303aa-8a26-4f80-9c7c-42d13ecc6348"
                                    }"""

                    ))),
            @ApiResponse(responseCode = "404", description = "Not found", content = @Content(mediaType = "application/json",
                    schema = @Schema(example =
                            """
                                    {
                                        "timestamp": "2023-08-25T05:38:33.335+00:00",
                                        "status": 404,
                                        "error": "Not Found",
                                        "message": "Category with id fd236x1e-8103-4c06-872c-c796262aa795 not found",
                                        "path": "/categories/fd236x1e-8103-4c06-872c-c796262aa795"
                                    }"""

                    )))
    })
    public ResponseEntity<CategoryResponse> getCategoryById(@PathVariable String categoryId, @RequestHeader HttpHeaders httpHeaders) {
        String acceptLanguage = httpHeaders.getFirst("Accept-Language");
        CategoryResponse categoryResponse = service.getCategoryById(categoryId, acceptLanguage);
        return ResponseEntity.ok(categoryResponse);
    }

    @PostMapping("/")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Success", content = @Content(mediaType = "application/json",
                    schema = @Schema(example =
                            """
                                    {
                                        "category_id": "a031ee28-4a62-4f76-a5e5-ca2acd81d384"
                                    }"""
                    ))),
            @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(mediaType = "application/json",
                    schema = @Schema(example =
                            """
                                    {
                                        "timestamp": "2023-08-25T05:45:25.423+00:00",
                                        "status": 400,
                                        "error": "Bad Request",
                                        "message": "400 BAD_REQUEST",
                                        "path": "/categories/",
                                        "fields": {
                                            "name": "Field name is required",
                                            "menu_id": "Menu id cannot be empty"
                                        }
                                    }"""
                    )))
    })
    public ResponseEntity<CategoryResponseId> createCategory(@RequestBody Category request, @RequestHeader HttpHeaders httpHeaders) {
        String acceptLanguage = httpHeaders.getFirst("Accept-Language");
        CategoryResponseId response = service.createCategory(request, acceptLanguage);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("{categoryId}")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "No Content"),
            @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(mediaType = "application/json",
                    schema = @Schema(example =
                            """
                                    {
                                        "timestamp": "2023-08-25T05:47:43.454+00:00",
                                        "status": 400,
                                        "error": "Bad Request",
                                        "message": "400 BAD_REQUEST",
                                        "path": "/categories/a031ee28-4a62-4f76-a5e5-ca2acd81d384",
                                        "fields": {
                                            "name": "Field name is required"
                                        }
                                    }"""
                    ))),
            @ApiResponse(responseCode = "404", description = "Not found", content = @Content(mediaType = "application/json",
                    schema = @Schema(example =
                            """
                                    {
                                        "timestamp": "2023-08-25T05:47:56.764+00:00",
                                        "status": 404,
                                        "error": "Not Found",
                                        "message": "Category with id a031ee28-4a6x-4f76-a5e5-ca2acd81d384 not found",
                                        "path": "/categories/a031ee28-4a6x-4f76-a5e5-ca2acd81d384"
                                    }"""
                    )))
    })
    public ResponseEntity<Void> patchCategoryById(@PathVariable String categoryId, @RequestBody
    @Schema(example = "{\n" +
            "  \"name\": \"string\"" +
            "}")
    Category request, @RequestHeader HttpHeaders httpHeaders) {
        String acceptLanguage = httpHeaders.getFirst("Accept-Language");
        service.patchCategoryById(categoryId, request, acceptLanguage);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{categoryId}")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "No Content"),
            @ApiResponse(responseCode = "404", description = "Not found", content = @Content(mediaType = "application/json",
                    schema = @Schema(example =
                            """
                                    {
                                        "timestamp": "2023-08-25T05:48:34.406+00:00",
                                        "status": 404,
                                        "error": "Not Found",
                                        "message": "Category with id fd301092-dx7b-4149-a4cc-dc96ae3bba3bx not found",
                                        "path": "/categories/fd301092-dx7b-4149-a4cc-dc96ae3bba3bx"
                                    }"""
                    )))
    })
    public ResponseEntity<Void> deleteCategoryById(@PathVariable String categoryId, @RequestHeader HttpHeaders httpHeaders) {
        String acceptLanguage = httpHeaders.getFirst("Accept-Language");
        service.deleteCategoryById(categoryId, acceptLanguage);
        return ResponseEntity.noContent().build();
    }

}
