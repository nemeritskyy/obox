package ua.com.obox.dbschema.language;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LanguageService {
    private final LanguageRepository languageRepository;

    public List<LanguageResponse> getAllLanguages() {
        List<Language> languageList;
        List<LanguageResponse> responses = new ArrayList<>();
        languageList = languageRepository.findAll();
        if (languageList.size() == 0) {
            languageList.add(Language.builder().name("English").label("en-US").createdAt(Instant.now().getEpochSecond()).updatedAt(Instant.now().getEpochSecond()).build());
            languageList.add(Language.builder().name("Українська").label("uk-UA").createdAt(Instant.now().getEpochSecond()).updatedAt(Instant.now().getEpochSecond()).build());
            languageList.add(Language.builder().name("Español").label("es-ES").createdAt(Instant.now().getEpochSecond()).updatedAt(Instant.now().getEpochSecond()).build());
            languageList.add(Language.builder().name("Français").label("fr-FR").createdAt(Instant.now().getEpochSecond()).updatedAt(Instant.now().getEpochSecond()).build());
            languageList.add(Language.builder().name("Deutsch").label("de-DE").createdAt(Instant.now().getEpochSecond()).updatedAt(Instant.now().getEpochSecond()).build());
            languageList.add(Language.builder().name("Italiano").label("it-IT").createdAt(Instant.now().getEpochSecond()).updatedAt(Instant.now().getEpochSecond()).build());
            languageList.add(Language.builder().name("Português").label("pt-PT").createdAt(Instant.now().getEpochSecond()).updatedAt(Instant.now().getEpochSecond()).build());
        }

        for (Language language : languageList) {
            responses.add(LanguageResponse.builder().languageId(language.getLanguageId()).name(language.getName()).label(language.getLabel()).build());
        }

        languageRepository.saveAll(languageList);
        return responses;
    }
}