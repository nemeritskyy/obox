package ua.com.obox.dbschema.menu;

import com.fasterxml.jackson.core.JsonProcessingException;
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

import static ua.com.obox.dbschema.tools.examples.MenuResponseExample.*;

@RestController
@RequestMapping("/menus")
@RequiredArgsConstructor
@Tag(name = "Menus")
public class MenuController {

    private final MenuService service;

    @GetMapping("/{menuId}/categories")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success", content = @Content(mediaType = "application/json",
                    schema = @Schema(example = GET_ALL_200_RESPONSE_EXAMPLE))),
            @ApiResponse(responseCode = "404", description = "Not found", content = @Content(mediaType = "application/json",
                    schema = @Schema(example = ALL_MAPPINGS_404_RESPONSE_EXAMPLE)))
    })
    public ResponseEntity<List<CategoryResponse>> getAllCategoriesByMenuId(@PathVariable String menuId, @RequestHeader HttpHeaders httpHeaders) {
        String acceptLanguage = httpHeaders.getFirst("Accept-Language");
        List<CategoryResponse> categoryResponses = service.getAllCategoriesByMenuId(menuId, acceptLanguage);
        return ResponseEntity.ok(categoryResponses);
    }

    @GetMapping("/{menuId}")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success", content = @Content(mediaType = "application/json",
                    schema = @Schema(example = GET_200_RESPONSE_EXAMPLE))),
            @ApiResponse(responseCode = "404", description = "Not found", content = @Content(mediaType = "application/json",
                    schema = @Schema(example = ALL_MAPPINGS_404_RESPONSE_EXAMPLE)))
    })
    public ResponseEntity<MenuResponse> getMenuById(@PathVariable String menuId, @RequestHeader HttpHeaders httpHeaders) throws JsonProcessingException {
        String acceptLanguage = httpHeaders.getFirst("Accept-Language");
        MenuResponse menuResponse = service.getMenuById(menuId, acceptLanguage);
        return ResponseEntity.ok(menuResponse);
    }

    @PostMapping("/")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Success", content = @Content(mediaType = "application/json",
                    schema = @Schema(example = POST_201_RESPONSE_EXAMPLE))),
            @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(mediaType = "application/json",
                    schema = @Schema(example = POST_400_RESPONSE_EXAMPLE)))
    })
    public ResponseEntity<MenuResponseId> createMenu(@RequestBody Menu request, @RequestHeader HttpHeaders httpHeaders) throws JsonProcessingException {
        String acceptLanguage = httpHeaders.getFirst("Accept-Language");
        MenuResponseId response = service.createMenu(request, acceptLanguage);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("{menuId}")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "No Content"),
            @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(mediaType = "application/json",
                    schema = @Schema(example = PATCH_400_RESPONSE_EXAMPLE))),
            @ApiResponse(responseCode = "404", description = "Not found", content = @Content(mediaType = "application/json",
                    schema = @Schema(example = ALL_MAPPINGS_404_RESPONSE_EXAMPLE)))
    })
    public ResponseEntity<Void> patchMenuById(@PathVariable String menuId, @RequestBody
    @Schema(example = "{\n" +
            "  \"name\": \"string\"" +
            "}")
    Menu request, @RequestHeader HttpHeaders httpHeaders) throws JsonProcessingException {
        String acceptLanguage = httpHeaders.getFirst("Accept-Language");
        service.patchMenuById(menuId, request, acceptLanguage);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{menuId}")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "No Content"),
            @ApiResponse(responseCode = "404", description = "Not found", content = @Content(mediaType = "application/json",
                    schema = @Schema(example = ALL_MAPPINGS_404_RESPONSE_EXAMPLE)))
    })
    public ResponseEntity<Void> deleteMenuById(@PathVariable String menuId, @RequestHeader HttpHeaders httpHeaders) {
        String acceptLanguage = httpHeaders.getFirst("Accept-Language");
        service.deleteMenuById(menuId, acceptLanguage);
        return ResponseEntity.noContent().build();
    }
}
