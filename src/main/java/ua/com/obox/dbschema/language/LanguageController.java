package ua.com.obox.dbschema.language;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
}
