package ua.com.obox.dbschema.translation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ua.com.obox.dbschema.tools.exception.ExceptionTools;
import ua.com.obox.dbschema.tools.translation.CheckHeader;
import ua.com.obox.dbschema.translation.responsebody.Content;

@Service
@RequiredArgsConstructor
public class TranslationService {
    private final TranslationRepository translationRepository;

    public TranslationResponse getAllTranslationById(String translationId, String acceptLanguage) throws JsonProcessingException {
        String finalAcceptLanguage = CheckHeader.checkHeaderLanguage(acceptLanguage);
        Translation translationFromDB = translationRepository.findAllByTranslationId(translationId).orElseThrow(() -> {
            ExceptionTools.notFoundResponse(".translationNotFound", finalAcceptLanguage, translationId);
            return null;
        });
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
