package ua.com.obox.dbschema.translation;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/translations")
@RequiredArgsConstructor
@Tag(name = "Translation")
public class TranslationController {
    private final TranslationService service;

    @GetMapping("/{translationId}")
    public ResponseEntity<TranslationResponse> getAllTranslationById(@PathVariable String translationId,
                                                                   @RequestHeader HttpHeaders httpHeaders) throws JsonProcessingException {
        String acceptLanguage = httpHeaders.getFirst("Accept-Language");
        TranslationResponse getAllTranslations = service.getAllTranslationById(translationId, acceptLanguage);
        return ResponseEntity.ok(getAllTranslations);
    }
}