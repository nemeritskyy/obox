package ua.com.obox.dbschema.menuitem;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ua.com.obox.dbschema.tools.Validator;
import ua.com.obox.dbschema.tools.exception.ExceptionTools;
import ua.com.obox.dbschema.tools.logging.LoggingService;

import java.io.IOException;

@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
@Tag(name = "Items")
public class MenuItemController {
    private final MenuItemService service;
    private final LoggingService loggingService;
    private String loggingMessage;

    @GetMapping("/{itemId}")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "404", description = "Not found")
    })
    public ResponseEntity<MenuItemResponse> getItemById(@PathVariable String itemId) {
        MenuItemResponse menuItemResponse = service.getItemById(itemId);
        return ResponseEntity.ok(menuItemResponse);
    }

    @PostMapping("/")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Success", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "400", description = "Bad Request")
    })
    public ResponseEntity<MenuItemResponseId> createItem(@RequestBody
                                                         @Schema(example = "{\n" +
                                                                 "  \"category_id\": \"uuid\",\n" +
                                                                 "  \"name\": \"string\",\n" +
                                                                 "  \"description\": \"string\",\n" +
                                                                 "  \"price\": 0,\n" +
                                                                 "  \"weight\": 0,\n" +
                                                                 "  \"calories\": 0,\n" +
                                                                 "  \"image\": \"Base64 only JPG and PNG (not necessary)\"\n" +
                                                                 "}")
                                                         MenuItem request) throws IOException {
        loggingMessage = ExceptionTools.generateLoggingMessage("createItem", request.getCategory_id());
        requestValidation(request);
        Validator.validateVarchar(loggingMessage, "Description", request.getDescription(), loggingService);
        MenuItemResponseId response = service.createItem(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("{itemId}")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "No Content"),
            @ApiResponse(responseCode = "404", description = "Not found")
    })
    public ResponseEntity<Void> patchItemById(@PathVariable String itemId, @RequestBody
    @Schema(example = "{\n" +
            "  \"name\": \"string\",\n" +
            "  \"description\": \"string\",\n" +
            "  \"price\": 0.0,\n" +
            "  \"category_id\": \"c8e7375d-1dbf-4d40-ae65-81cd1e6e973f\",\n" +
            "  \"visibility\": true\n" +
            "}")
    MenuItem request) {
        loggingMessage = ExceptionTools.generateLoggingMessage("patchItemById", request.getCategory_id());
        requestValidation(request);
        service.patchItemById(itemId, request);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{itemId}")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "No Content"),
            @ApiResponse(responseCode = "404", description = "Not found")
    })
    public ResponseEntity<Void> deleteMenuById(@PathVariable String itemId) {
        service.deleteItemById(itemId);
        return ResponseEntity.noContent().build();
    }

    private void requestValidation(MenuItem request) {
        Validator.validateName(loggingMessage, request.getName(), loggingService);
        Validator.checkUUID(loggingMessage, request.getCategory_id(), loggingService); // validate UUID
        Validator.positiveInteger("Price", request.getPrice().intValue(), 100000, loggingService); // validate price
        Validator.positiveInteger("Calories", request.getCalories(), 30000, loggingService); // validate calories
        Validator.positiveInteger("Weight", request.getWeight(), 100000, loggingService); // validate weight
    }
}
