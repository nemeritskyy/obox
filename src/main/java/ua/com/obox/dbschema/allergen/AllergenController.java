package ua.com.obox.dbschema.allergen;

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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static ua.com.obox.dbschema.tools.examples.AllergenResponseExample.*;

@RestController
@RequestMapping("/allergens/")
@PreAuthorize("hasAnyRole('ADMIN','USER')")
@RequiredArgsConstructor
@Tag(name = "Allergens")
public class AllergenController {
    private final AllergenService service;

    @GetMapping("/{restaurantId}/restaurant-allergens")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success", content = @Content(mediaType = "application/json",
                    schema = @Schema(example = GET_ALL_200_RESPONSE_EXAMPLE))),
            @ApiResponse(responseCode = "404", description = "Not found", content = @Content(mediaType = "application/json",
                    schema = @Schema(example = ALL_MAPPINGS_404_RESPONSE_EXAMPLE)))
    })
    public ResponseEntity<List<AllergenResponse>> getAllAllergensByRestaurantId(@PathVariable String restaurantId, @RequestHeader HttpHeaders httpHeaders) {
        String acceptLanguage = httpHeaders.getFirst("Accept-Language");
        List<AllergenResponse> menuResponses = service.getAllAllergensByRestaurantId(restaurantId, acceptLanguage);
        return ResponseEntity.ok(menuResponses);
    }

    @GetMapping("/{allergenId}")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success", content = @Content(mediaType = "application/json",
                    schema = @Schema(example = GET_200_RESPONSE_EXAMPLE))),
            @ApiResponse(responseCode = "404", description = "Not found", content = @Content(mediaType = "application/json",
                    schema = @Schema(example = ALL_MAPPINGS_404_RESPONSE_EXAMPLE)))
    })
    public ResponseEntity<AllergenResponse> getAllergenById(@PathVariable String allergenId, @RequestHeader HttpHeaders httpHeaders) throws JsonProcessingException {
        String acceptLanguage = httpHeaders.getFirst("Accept-Language");
        AllergenResponse allergenResponse = service.getAllergenById(allergenId, acceptLanguage);
        return ResponseEntity.ok(allergenResponse);
    }

    @PostMapping("/")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "No Content"),
            @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(mediaType = "application/json",
                    schema = @Schema(example = POST_400_RESPONSE_EXAMPLE))),
            @ApiResponse(responseCode = "404", description = "Not found", content = @Content(mediaType = "application/json",
                    schema = @Schema(example = ALL_MAPPINGS_404_RESPONSE_EXAMPLE)))
    })
    public ResponseEntity<AllergenResponseId> createAllergen(@RequestBody
                                                             @Schema(example = POST_BODY)
                                                             Allergen request, @RequestHeader HttpHeaders httpHeaders) throws JsonProcessingException {
        String acceptLanguage = httpHeaders.getFirst("Accept-Language");
        AllergenResponseId response = service.createAllergen(request, acceptLanguage);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("{allergenId}")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "No Content"),
            @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(mediaType = "application/json",
                    schema = @Schema(example = PATCH_400_RESPONSE_EXAMPLE))),
            @ApiResponse(responseCode = "404", description = "Not found", content = @Content(mediaType = "application/json",
                    schema = @Schema(example = ALL_MAPPINGS_404_RESPONSE_EXAMPLE)))
    })
    public ResponseEntity<Void> patchAllergenById(@PathVariable String allergenId, @RequestBody
    @Schema(example = PATCH_BODY)
    Allergen request, @RequestHeader HttpHeaders httpHeaders) throws JsonProcessingException {
        String acceptLanguage = httpHeaders.getFirst("Accept-Language");
        service.patchAllergenById(allergenId, request, acceptLanguage);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{allergenId}")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "No Content"),
            @ApiResponse(responseCode = "404", description = "Not found", content = @Content(mediaType = "application/json",
                    schema = @Schema(example = ALL_MAPPINGS_404_RESPONSE_EXAMPLE)))
    })
    public ResponseEntity<Void> deleteAllergenById(@PathVariable String allergenId, @RequestHeader HttpHeaders httpHeaders) {
        String acceptLanguage = httpHeaders.getFirst("Accept-Language");
        service.deleteAllergenById(allergenId, acceptLanguage);
        return ResponseEntity.noContent().build();
    }
}
