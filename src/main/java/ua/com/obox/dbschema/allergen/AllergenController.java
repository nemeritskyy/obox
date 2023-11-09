package ua.com.obox.dbschema.allergen;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/allergens/")
@RequiredArgsConstructor
@Tag(name = "Allergens")
public class AllergenController {
    private final AllergenService service;

    @PostMapping("/")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "No Content"),
            @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(mediaType = "application/json",
                    schema = @Schema(example = ""))),
            @ApiResponse(responseCode = "404", description = "Not found", content = @Content(mediaType = "application/json",
                    schema = @Schema(example = "")))
    })
    public ResponseEntity<Void> createMenu(@RequestBody @Schema(example = "") Allergen request, @RequestHeader HttpHeaders httpHeaders) throws JsonProcessingException {
        String acceptLanguage = httpHeaders.getFirst("Accept-Language");
        service.addAllergen(request, acceptLanguage);
        return ResponseEntity.noContent().build();
    }
}
