package ua.com.obox.dbschema.translation;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import static ua.com.obox.dbschema.tools.examples.TranslationResponseExample.*;

@RestController
@RequestMapping("/translations")
@PreAuthorize("hasAnyRole('ADMIN','USER')")
@RequiredArgsConstructor
@Tag(name = "Translation")
public class TranslationController {
    private final TranslationService service;

    @GetMapping("/{translationId}")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success", content = @Content(mediaType = "application/json",
                    schema = @Schema(example = GET_200_RESPONSE_EXAMPLE))),
            @ApiResponse(responseCode = "404", description = "Not found", content = @Content(mediaType = "application/json",
                    schema = @Schema(example = ALL_MAPPINGS_404_RESPONSE_EXAMPLE)))
    })
    public ResponseEntity<TranslationResponse> getAllTranslationById(@PathVariable String translationId,
                                                                   @RequestHeader HttpHeaders httpHeaders) throws JsonProcessingException {
        String acceptLanguage = httpHeaders.getFirst("Accept-Language");
        TranslationResponse getAllTranslations = service.getAllTranslationById(translationId, acceptLanguage);
        return ResponseEntity.ok(getAllTranslations);
    }
}