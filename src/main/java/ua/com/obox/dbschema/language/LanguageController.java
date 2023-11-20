package ua.com.obox.dbschema.language;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/languages")
@RequiredArgsConstructor
@Hidden
@Tag(name = "Languages")
public class LanguageController {
    private final LanguageService service;

    @GetMapping("/{tenantId}")
    public ResponseEntity<List<Language>> getAllLanguagesByTenantId(@PathVariable String tenantId,
                                                                    @RequestHeader HttpHeaders httpHeaders) {
        String acceptLanguage = httpHeaders.getFirst("Accept-Language");
        List<Language> getAllLanguagesByTenantId = service.getAllLanguages(tenantId, acceptLanguage);
        return ResponseEntity.ok(getAllLanguagesByTenantId);
    }
}
