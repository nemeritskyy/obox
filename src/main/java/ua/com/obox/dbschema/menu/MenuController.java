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
                            "[\n" +
                                    "    {\n" +
                                    "        \"category_id\": \"dec66073-063c-4c58-ac97-2aab5b559dd4\",\n" +
                                    "        \"name\": \"Fresh\",\n" +
                                    "        \"menu_id\": \"05b303aa-8a26-4f80-9c7c-42d13ecc6348\"\n" +
                                    "    },\n" +
                                    "    {\n" +
                                    "        \"category_id\": \"fd236b1e-8103-4c06-872c-c796262aa795\",\n" +
                                    "        \"name\": \"Vegetables\",\n" +
                                    "        \"menu_id\": \"05b303aa-8a26-4f80-9c7c-42d13ecc6348\"\n" +
                                    "    }\n" +
                                    "]"

                    ))),
            @ApiResponse(responseCode = "404", description = "Not found", content = @Content(mediaType = "application/json",
                    schema = @Schema(example =
                            "{\n" +
                                    "    \"timestamp\": \"2023-08-24T12:07:03.855+00:00\",\n" +
                                    "    \"status\": 404,\n" +
                                    "    \"error\": \"Not Found\",\n" +
                                    "    \"message\": \"Menu with id 05b303aa-8a26-xf80-9c7c-42d13ecc6348 not found\",\n" +
                                    "    \"path\": \"/menus/05b303aa-8a26-xf80-9c7c-42d13ecc6348/categories\"\n" +
                                    "}"

                    )))
    })
    public ResponseEntity<List<CategoryResponse>> getAllCategoriesByMenuId(@PathVariable String menuId) {
        List<CategoryResponse> categoryResponses = service.getAllCategoriesByMenuId(menuId);
        return ResponseEntity.ok(categoryResponses);
    }

    @GetMapping("/{menuId}")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success", content = @Content(mediaType = "application/json",
                    schema = @Schema(example =
                            "{\n" +
                                    "    \"menu_id\": \"05b303aa-8a26-4f80-9c7c-42d13ecc6348\",\n" +
                                    "    \"restaurant_id\": \"9aff3e00-451c-490e-b48b-c4315785b75e\",\n" +
                                    "    \"name\": \"Non alcohol bar\",\n" +
                                    "    \"language_code\": \"en\"\n" +
                                    "}"

                    ))),
            @ApiResponse(responseCode = "404", description = "Not found", content = @Content(mediaType = "application/json",
                    schema = @Schema(example =
                            "{\n" +
                                    "    \"timestamp\": \"2023-08-25T05:10:43.103+00:00\",\n" +
                                    "    \"status\": 404,\n" +
                                    "    \"error\": \"Not Found\",\n" +
                                    "    \"message\": \"Menu with id 05b303aa-8a26-4fx0-9c7c-42d13ecc6348 not found\",\n" +
                                    "    \"path\": \"/menus/05b303aa-8a26-4fx0-9c7c-42d13ecc6348\"\n" +
                                    "}"

                    )))
    })
    public ResponseEntity<MenuResponse> getMenuById(@PathVariable String menuId) {
        MenuResponse menuResponse = service.getMenuById(menuId);
        return ResponseEntity.ok(menuResponse);
    }

    @PostMapping("/")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Success", content = @Content(mediaType = "application/json",
                    schema = @Schema(example =
                            "{\n" +
                                    "    \"menu_id\": \"b4d331ec-ec54-4789-b8f3-1b3435eaa52a\"\n" +
                                    "}"
                    ))),
            @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(mediaType = "application/json",
                    schema = @Schema(example =
                            "{\n" +
                                    "    \"timestamp\": \"2023-08-25T05:11:14.711+00:00\",\n" +
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
    public ResponseEntity<MenuResponseId> createMenu(@RequestBody Menu request) {
        MenuResponseId response = service.createMenu(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("{menuId}")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "No Content"),
            @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(mediaType = "application/json",
                    schema = @Schema(example =
                            "{\n" +
                                    "    \"timestamp\": \"2023-08-25T05:15:40.318+00:00\",\n" +
                                    "    \"status\": 400,\n" +
                                    "    \"error\": \"Bad Request\",\n" +
                                    "    \"message\": \"400 BAD_REQUEST\",\n" +
                                    "    \"path\": \"/menus/b4d331ec-ec54-4789-b8f3-1b3435eaa52a\",\n" +
                                    "    \"fields\": {\n" +
                                    "        \"name\": \"Field name is required\"\n" +
                                    "    }\n" +
                                    "}"
                    ))),
            @ApiResponse(responseCode = "404", description = "Not found", content = @Content(mediaType = "application/json",
                    schema = @Schema(example =
                            "{\n" +
                                    "    \"timestamp\": \"2023-08-25T05:15:14.520+00:00\",\n" +
                                    "    \"status\": 404,\n" +
                                    "    \"error\": \"Not Found\",\n" +
                                    "    \"message\": \"Menu with id b4d331ec-ec54-47x9-b8f3-1b3435eaa52a not found\",\n" +
                                    "    \"path\": \"/menus/b4d331ec-ec54-47x9-b8f3-1b3435eaa52a\"\n" +
                                    "}"
                    )))
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
            @ApiResponse(responseCode = "404", description = "Not found", content = @Content(mediaType = "application/json",
                    schema = @Schema(example =
                            "{\n" +
                                    "    \"timestamp\": \"2023-08-25T05:16:10.781+00:00\",\n" +
                                    "    \"status\": 404,\n" +
                                    "    \"error\": \"Not Found\",\n" +
                                    "    \"message\": \"Menu with id 3385cf94-d26e-4c72-acx6-19b2a5e19982 not found\",\n" +
                                    "    \"path\": \"/menus/3385cf94-d26e-4c72-acx6-19b2a5e19982\"\n" +
                                    "}"
                    )))
    })
    public ResponseEntity<Void> deleteMenuById(@PathVariable String menuId) {
        service.deleteMenuById(menuId);
        return ResponseEntity.noContent().build();
    }
}
