package ua.com.obox.dbschema.translation.assistant;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import ua.com.obox.dbschema.tools.configuration.ValidationConfiguration;
import ua.com.obox.dbschema.translation.Translation;
import ua.com.obox.dbschema.translation.TranslationRepository;
import ua.com.obox.dbschema.translation.responsebody.Content;
import ua.com.obox.dbschema.translation.responsebody.MenuTranslationEntry;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;

@AllArgsConstructor
public class ExistEntity<T extends MenuTranslationEntry> {
    private final ResourceBundle translation = ResourceBundle.getBundle("translation.messages");
    private final TranslationRepository translationRepository;

    public void checkExistEntity(String name, List<? extends IdentifiableId> listParentEntries, String errorLanguage, Map<String, String> fieldErrors) {
        ObjectMapper objectMapper = new ObjectMapper();
        List<String> supportLanguages = ValidationConfiguration.SUPPORT_LANGUAGES;
        if (!listParentEntries.isEmpty()) {
            Content<T> content;
            T translationEntry;
            for (IdentifiableId element : listParentEntries) {
                Optional<Translation> translationOptional = translationRepository.findAllByReferenceId(element.getId());
                if (translationOptional.isPresent()) {
                    Translation translationExist = translationOptional.get();
                    try {
                        content = objectMapper.readValue(translationExist.getContent(), new TypeReference<>() {
                        });
                        for (String entryLanguage : supportLanguages) {
                            translationEntry = content.getContent().get(entryLanguage);
                            if (translationEntry != null && translationEntry.getName().equals(name)) {
                                fieldErrors.put("name", translation.getString(errorLanguage + ".nameExists"));
                            }
                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
