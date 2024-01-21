package ua.com.obox.dbschema.dish;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ua.com.obox.dbschema.tools.logging.LogLevel;
import ua.com.obox.dbschema.tools.logging.LoggingService;
import ua.com.obox.dbschema.tools.response.BadFieldsResponse;

import java.util.HashMap;
import java.util.Map;

import static ua.com.obox.dbschema.tools.examples.DishResponseExample.*;

@RestController
@RequestMapping("/dishes")
@PreAuthorize("hasAnyRole('ADMIN','USER')")
@RequiredArgsConstructor
@Tag(name = "Dishes")
public class DishController {
    private final DishService service;
    private final LoggingService loggingService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @GetMapping("/{dishId}")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success", content = @Content(mediaType = "application/json",
                    schema = @Schema(example = GET_200_RESPONSE_EXAMPLE))),
            @ApiResponse(responseCode = "404", description = "Not found", content = @Content(mediaType = "application/json",
                    schema = @Schema(example = ALL_MAPPINGS_404_RESPONSE_EXAMPLE)))
    })
    public ResponseEntity<DishResponse> getDishById(
            @PathVariable String dishId,
            @RequestHeader HttpHeaders httpHeaders) throws JsonProcessingException {
        String acceptLanguage = httpHeaders.getFirst("Accept-Language");
        DishResponse dishResponse = service.getDishById(dishId, acceptLanguage);
        return ResponseEntity.ok(dishResponse);
    }

    @PostMapping("/")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Success", content = @Content(mediaType = "application/json",
                    schema = @Schema(example = POST_201_RESPONSE_EXAMPLE))),
            @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(mediaType = "application/json",
                    schema = @Schema(example = POST_400_RESPONSE_EXAMPLE)))
    })
    public ResponseEntity<DishResponseId> createDish(
            @RequestBody @Schema(example = POST_BODY) String jsonRequest,
            @RequestHeader HttpHeaders httpHeaders) throws JsonProcessingException {
        String acceptLanguage = httpHeaders.getFirst("Accept-Language");
        DishResponseId response = service.createDish(validateJSON(jsonRequest), acceptLanguage);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Hidden
    @PostMapping("/{dishId}/set-primary-image")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "No Content"),
            @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(mediaType = "application/json",
                    schema = @Schema(example = POST_ADD_PRIMARY_IMAGE_400))),
            @ApiResponse(responseCode = "404", description = "Not found", content = @Content(mediaType = "application/json",
                    schema = @Schema(example = ALL_MAPPINGS_404_RESPONSE_EXAMPLE)))
    })
    public ResponseEntity<DishResponseId> setPrimaryImage(
            @RequestBody @Schema(example = POST_ADD_PRIMARY_IMAGE) Dish request,
            @RequestHeader HttpHeaders httpHeaders,
            @PathVariable String dishId) {
        String acceptLanguage = httpHeaders.getFirst("Accept-Language");
        service.setPrimaryImage(request, dishId, acceptLanguage);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("{dishId}")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "No Content"),
            @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(mediaType = "application/json",
                    schema = @Schema(example = PATCH_400_RESPONSE_EXAMPLE))),
            @ApiResponse(responseCode = "404", description = "Not found", content = @Content(mediaType = "application/json",
                    schema = @Schema(example = ALL_MAPPINGS_404_RESPONSE_EXAMPLE)))
    })
    public ResponseEntity<Void> patchDishById(
            @RequestBody @Schema(example = PATCH_BODY) String jsonRequest,
            @PathVariable String dishId,
            @RequestHeader HttpHeaders httpHeaders) throws JsonProcessingException {
        String acceptLanguage = httpHeaders.getFirst("Accept-Language");
        service.patchDishById(dishId, validateJSON(jsonRequest), acceptLanguage);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{dishId}")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "No Content"),
            @ApiResponse(responseCode = "404", description = "Not found", content = @Content(mediaType = "application/json",
                    schema = @Schema(example = ALL_MAPPINGS_404_RESPONSE_EXAMPLE)))
    })
    public ResponseEntity<Void> deleteDishById(
            @PathVariable String dishId,
            @RequestHeader HttpHeaders httpHeaders) {
        String acceptLanguage = httpHeaders.getFirst("Accept-Language");
        service.deleteDishById(dishId, acceptLanguage);
        return ResponseEntity.noContent().build();
    }

    private Dish validateJSON(String jsonRequest) {
        Dish dish;
        try {
            dish = objectMapper.readValue(jsonRequest, Dish.class);
        } catch (Exception e) {
            loggingService.log(LogLevel.JSON, jsonRequest);
            Map<String, String> responseBody = new HashMap<>();
            responseBody.put("error", "Contact the administrator to resolve the problem");
            throw new BadFieldsResponse(HttpStatus.BAD_REQUEST, responseBody);
        }
        return dish;
    }
}

