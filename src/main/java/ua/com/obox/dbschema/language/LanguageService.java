package ua.com.obox.dbschema.language;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ua.com.obox.dbschema.tools.logging.LoggingService;

import java.util.List;
import java.util.ResourceBundle;

@Service
@RequiredArgsConstructor
public class LanguageService {
    private final LoggingService loggingService;
    private final ResourceBundle translation = ResourceBundle.getBundle("translation.messages");
    private final LanguageRepository languageRepository;

    public List<Language> getAllLanguages(String tenantId, String acceptLanguage) {
//        String finalAcceptLanguage = CheckHeader.checkHeaderLanguage(acceptLanguage);
        List<Language> languageList;
        languageList = languageRepository.findAllByTenant_TenantId(tenantId);
        return languageList;
    }
}