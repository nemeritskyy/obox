package ua.com.obox.dbschema.dish;

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

@RestController
@RequestMapping("/dishes")
@RequiredArgsConstructor
@Tag(name = "Dishes")
public class DishController {
    private final DishService service;
    private final LoggingService loggingService;
    private String loggingMessage;

    @GetMapping("/{dishId}")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "404", description = "Not found")
    })
    public ResponseEntity<DishResponse> getDishById(@PathVariable String dishId) {
        DishResponse dishResponse = service.getDishById(dishId);
        return ResponseEntity.ok(dishResponse);
    }

    @PostMapping("/")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Success", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "400", description = "Bad Request")
    })
    public ResponseEntity<DishResponseId> createDish(@RequestBody
                                                         @Schema(example = "{\n" +
                                                                 "  \"category_id\": \"uuid\",\n" +
                                                                 "  \"name\": \"string\",\n" +
                                                                 "  \"description\": \"string\",\n" +
                                                                 "  \"price\": 0,\n" +
                                                                 "  \"weight\": 0,\n" +
                                                                 "  \"calories\": 0,\n" +
                                                                 "  \"allergens\": \"Celery::Cereals containing gluten::Clams\",\n" +
                                                                 "  \"tags\": \"Spicy::Vegan\",\n" +
                                                                 "  \"state\": \"ENABLED or DISABLED\",\n" +
                                                                 "  \"image\": \"Base64 only JPG and PNG (not necessary)\"\n" +
                                                                 "}")
                                                             Dish request) {
        loggingMessage = ExceptionTools.generateLoggingMessage("createDish", request.getCategory_id());
        Validator.validateState(loggingMessage, request.getState(), loggingService);
        DishResponseId response = service.createDish(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("{dishId}")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "No Content"),
            @ApiResponse(responseCode = "404", description = "Not found")
    })
    public ResponseEntity<Void> patchDishById(@PathVariable String dishId, @RequestBody

    Dish request) {
        loggingMessage = ExceptionTools.generateLoggingMessage("patchDishById", request.getCategory_id());
        service.patchDishById(dishId, request);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{dishId}")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "No Content"),
            @ApiResponse(responseCode = "404", description = "Not found")
    })
    public ResponseEntity<Void> deleteDishById(@PathVariable String dishId) {
        service.deleteDishById(dishId);
        return ResponseEntity.noContent().build();
    }
}
