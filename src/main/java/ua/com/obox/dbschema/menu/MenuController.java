package ua.com.obox.dbschema.menu;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ua.com.obox.dbschema.category.CategoryResponse;
import ua.com.obox.dbschema.tools.logging.LoggingService;

import java.util.List;

@RestController
@RequestMapping("/menus")
@RequiredArgsConstructor
@Tag(name = "Menus")
public class MenuController {

    private final MenuService service;
    private final LoggingService loggingService;

    @GetMapping("/{menuId}/categories")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "404", description = "Not found")
    })
    public ResponseEntity<List<CategoryResponse>> getAllCategoriesByMenuId(@PathVariable String menuId) {
        List<CategoryResponse> categoryResponses = service.getAllCategoriesByMenuId(menuId);
        return ResponseEntity.ok(categoryResponses);
    }

    @GetMapping("/{menuId}")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "404", description = "Not found")
    })
    public ResponseEntity<MenuResponse> getMenuById(@PathVariable String menuId) {
        MenuResponse menuResponse = service.getMenuById(menuId);
        return ResponseEntity.ok(menuResponse);
    }

    @PostMapping("/")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Success", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "400", description = "Bad Request")
    })
    public ResponseEntity<MenuResponseId> createMenu(@RequestBody Menu request) {
        MenuResponseId response = service.createMenu(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("{menuId}")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "No Content"),
            @ApiResponse(responseCode = "404", description = "Not found")
    })
    public ResponseEntity<Void> patchMenuById(@PathVariable String menuId, @RequestBody
    @Schema(example = "{\n" +
            "  \"name\": \"string\"" +
            "}")
    Menu request) {
        service.patchMenuById(menuId, request);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{menuId}")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "No Content"),
            @ApiResponse(responseCode = "404", description = "Not found")
    })
    public ResponseEntity<Void> deleteMenuById(@PathVariable String menuId) {
        service.deleteMenuById(menuId);
        return ResponseEntity.noContent().build();
    }
}
