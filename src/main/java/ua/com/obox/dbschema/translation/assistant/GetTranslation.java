package ua.com.obox.dbschema.translation.assistant;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ua.com.obox.dbschema.category.CategoryRepository;
import ua.com.obox.dbschema.menu.MenuRepository;
import ua.com.obox.dbschema.tools.exception.ExceptionTools;
import ua.com.obox.dbschema.translation.Translation;
import ua.com.obox.dbschema.translation.TranslationRepository;
import ua.com.obox.dbschema.translation.responsebody.Content;

@Service
@RequiredArgsConstructor
public class GetTranslation {
    private final TranslationRepository translationRepository;

    public String getParentNameByEntityId(String entityId, String translationLanguage, String errorLanguage) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        OnlyName onlyName;
        Content<OnlyName> onlyNameContent;
        Translation translation = translationRepository.findAllByReferenceId(entityId).orElseThrow(null);
        if (translation == null) {
            ExceptionTools.notFoundResponse(".translationNotFound", errorLanguage, entityId);
        } else {
            onlyNameContent = objectMapper.readValue(translation.getContent(), new TypeReference<>() {
            });
            onlyName = onlyNameContent.getContent().get(translationLanguage);
            if (onlyName != null && onlyName.getName() != null)
                return onlyName.getName();
        }
        return "...";
    }
}
