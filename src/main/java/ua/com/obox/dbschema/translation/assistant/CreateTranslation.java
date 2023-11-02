package ua.com.obox.dbschema.translation.assistant;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ua.com.obox.dbschema.translation.Translation;
import ua.com.obox.dbschema.translation.TranslationRepository;
import ua.com.obox.dbschema.translation.responsebody.Content;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CreateTranslation<T> {
    private final TranslationRepository translationRepository;

    public Translation create(String referenceId, String typeReference, String language, T entry) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();

        Content<T> content = Content.<T>builder()
                .content(new HashMap<>())
                .build();
        Map<String, T> languagesMap = content.getContent();
        languagesMap.put(language, entry);

        Translation translation = Translation.builder().build();
        translation.setReferenceId(referenceId);
        translation.setReferenceType(typeReference);
        translation.setContent(objectMapper.writeValueAsString(content));
        translation.setCreatedAt(Instant.now().getEpochSecond());
        translation.setUpdatedAt(Instant.now().getEpochSecond());

        translationRepository.save(translation);
        return translation;
    }
}