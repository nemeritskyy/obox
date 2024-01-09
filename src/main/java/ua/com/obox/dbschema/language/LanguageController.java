package ua.com.obox.dbschema.language;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static ua.com.obox.dbschema.tools.examples.LanguageResponseExample.*;


@RestController
@RequestMapping("/languages")
@RequiredArgsConstructor
@Tag(name = "Languages")
public class LanguageController {
    private final LanguageService service;

    @GetMapping("/")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success", content = @Content(mediaType = "application/json",
                    schema = @Schema(example = GET_ALL_200_RESPONSE_EXAMPLE))),
    })
    public ResponseEntity<List<LanguageResponse>> getAllLanguagesByTenantId() {
        List<LanguageResponse> getAllLanguages = service.getAllLanguages();
        return ResponseEntity.ok(getAllLanguages);
    }

    @PostMapping("/")
    @GetMapping("/")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "No Content"),
            @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(mediaType = "application/json",
                    schema = @Schema(example = POST_400_RESPONSE_EXAMPLE))),
            @ApiResponse(responseCode = "404", description = "Not found", content = @Content(mediaType = "application/json",
                    schema = @Schema(example = ALL_MAPPINGS_404_RESPONSE_EXAMPLE)))
    })
    public ResponseEntity<Void> postLanguagesByRestaurantId(@RequestBody @Schema(example = POST_BODY) SelectedLanguage selectedLanguage, @RequestHeader HttpHeaders httpHeaders) {
        String acceptLanguage = httpHeaders.getFirst("Accept-Language");
        service.addLanguageForRestaurant(selectedLanguage, acceptLanguage);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{restaurantId}")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success", content = @Content(mediaType = "application/json",
                    schema = @Schema(example = GET_RESTAURANT_200_RESPONSE_EXAMPLE))),
            @ApiResponse(responseCode = "404", description = "Not found", content = @Content(mediaType = "application/json",
                    schema = @Schema(example = ALL_MAPPINGS_404_RESPONSE_EXAMPLE)))
    })
    public ResponseEntity<List<LanguageResponse>> getLanguagesByRestaurantId(
            @PathVariable String restaurantId,
            @RequestHeader HttpHeaders httpHeaders) {
        String acceptLanguage = httpHeaders.getFirst("Accept-Language");
        List<LanguageResponse> languageList = service.getLanguagesByRestaurantId(restaurantId, acceptLanguage);
        return ResponseEntity.ok(languageList);
    }

    @DeleteMapping("/{restaurantId}/{languageId}")
    public ResponseEntity<Void> removeLanguageFromRestaurant(@PathVariable String restaurantId, @PathVariable String languageId, @RequestHeader HttpHeaders httpHeaders) {
        String acceptLanguage = httpHeaders.getFirst("Accept-Language");
        service.removeLanguageFromRestaurant(restaurantId, languageId, acceptLanguage);
        return ResponseEntity.noContent().build();
    }
}
