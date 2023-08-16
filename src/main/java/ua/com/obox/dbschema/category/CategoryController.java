package ua.com.obox.dbschema.category;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
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
            @ApiResponse(responseCode = "200", description = "Success", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "404", description = "Not found")
    })
    public ResponseEntity<List<DishResponse>> getAllDishesByCategoryId(@PathVariable String categoryId) {
        List<DishResponse> categoryResponses = service.getAllDishesByCategoryId(categoryId);
        return ResponseEntity.ok(categoryResponses);
    }

    @GetMapping("/{categoryId}")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "404", description = "Not found")
    })
    public ResponseEntity<CategoryResponse> getCategoryById(@PathVariable String categoryId) {
        CategoryResponse categoryResponse = service.getCategoryById(categoryId);
        return ResponseEntity.ok(categoryResponse);
    }

    @PostMapping("/")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Success", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "400", description = "Bad Request")
    })
    public ResponseEntity<CategoryResponseId> createCategory(@RequestBody Category request) {
        CategoryResponseId response = service.createCategory(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("{categoryId}")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "No Content"),
            @ApiResponse(responseCode = "404", description = "Not found")
    })
    public ResponseEntity<Void> patchCategoryById(@PathVariable String categoryId, @RequestBody
    @Schema(example = "{\n" +
            "  \"name\": \"string\"" +
            "}")
    Category request) {
        service.patchCategoryById(categoryId, request);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{categoryId}")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "No Content"),
            @ApiResponse(responseCode = "404", description = "Not found")
    })
    public ResponseEntity<Void> deleteCategoryById(@PathVariable String categoryId) {
        service.deleteCategoryById(categoryId);
        return ResponseEntity.noContent().build();
    }

}
