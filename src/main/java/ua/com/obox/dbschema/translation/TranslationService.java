package ua.com.obox.dbschema.translation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ua.com.obox.dbschema.tools.logging.LoggingService;
import ua.com.obox.dbschema.translation.responsebody.Content;

import java.util.ResourceBundle;

@Service
@RequiredArgsConstructor
public class TranslationService {
    private final LoggingService loggingService;
    private final ResourceBundle translation = ResourceBundle.getBundle("translation.messages");
    private final TranslationRepository translationRepository;

    public TranslationResponse getAllTranslationById(String translationId, String acceptLanguage) throws JsonProcessingException {
        var translationInfo = translationRepository.findAllByTranslationId(translationId);
        Translation translationFromDB = translationInfo.orElseThrow(() -> null);
        assert translationFromDB != null;

        TranslationResponse translationResponse = TranslationResponse.builder().build();
        ObjectMapper objectMapper = new ObjectMapper();

        translationResponse.setTranslationId(translationFromDB.getTranslationId());
        translationResponse.setReferenceId(translationFromDB.getReferenceId());
        translationResponse.setReferenceType(translationFromDB.getReferenceType());
        translationResponse.setContent(objectMapper.readValue(translationFromDB.getContent(), Content.class));
        return translationResponse;
    }
}
