package ua.com.obox.dbschema.language;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ua.com.obox.dbschema.tools.CatchResponse;

import java.util.List;

@RestController
@RequestMapping("/languages")
@RequiredArgsConstructor
@Tag(name = "Languages")
public class LanguageController {
    private final LanguageService service;

    @GetMapping("/")
    public ResponseEntity<List<LanguageResponse>> getAllLanguagesByTenantId() {
        List<LanguageResponse> getAllLanguages = service.getAllLanguages();
        return ResponseEntity.ok(getAllLanguages);
    }

    @PostMapping("/set-languages")
    public ResponseEntity<Void> getLanguagesByRestaurantId(@RequestBody SelectedLanguages selectedLanguages, @RequestHeader HttpHeaders httpHeaders) {
        String acceptLanguage = httpHeaders.getFirst("Accept-Language");
        try {
            service.getAllLanguagesForRestaurant(selectedLanguages, acceptLanguage);
        } catch (Exception e) {
            CatchResponse.getMessage();
        }
        return ResponseEntity.noContent().build();
    }
}
