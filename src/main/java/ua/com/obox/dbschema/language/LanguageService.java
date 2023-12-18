package ua.com.obox.dbschema.language;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import ua.com.obox.dbschema.restaurant.RestaurantRepository;
import ua.com.obox.dbschema.tools.configuration.ValidationConfiguration;
import ua.com.obox.dbschema.tools.exception.ExceptionTools;
import ua.com.obox.dbschema.tools.response.BadFieldsResponse;
import ua.com.obox.dbschema.tools.response.ResponseErrorMap;
import ua.com.obox.dbschema.tools.translation.CheckHeader;

import java.time.Instant;
import java.util.*;

@Service
@RequiredArgsConstructor
public class LanguageService {
    private final LanguageRepository languageRepository;
    private final SelectedLanguagesRepository selectedLanguagesRepository;
    private final RestaurantRepository restaurantRepository;
    private static final ResourceBundle translation = ResourceBundle.getBundle("translation.messages");

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

    public void postLanguagesForRestaurant(SelectedLanguages request, String acceptLanguage) {
        String finalAcceptLanguage = CheckHeader.checkHeaderLanguage(acceptLanguage);
        restaurantRepository.findByRestaurantId(request.getRestaurantId()).orElseThrow(() -> ExceptionTools.notFoundException(".restaurantNotFound", finalAcceptLanguage, request.getRestaurantId()));
        Map<String, String> fieldErrors = new ResponseErrorMap<>();
        if (!String.join(",", request.getLanguagesArray()).matches(ValidationConfiguration.UUID_REGEX)) {
            fieldErrors.put("languages", translation.getString(finalAcceptLanguage + ".badSortedList"));
        }
        if (fieldErrors.size() > 0)
            throw new BadFieldsResponse(HttpStatus.BAD_REQUEST, fieldErrors);

        SelectedLanguages selectedLanguages = selectedLanguagesRepository.findByRestaurantId(request.getRestaurantId()).orElse(null);
        if (selectedLanguages != null) {
            List<String> removedLanguagesId = new ArrayList<>(Arrays.asList(selectedLanguages.getLanguagesList().split(",")));
            List<String> newList = Arrays.asList(request.getLanguagesArray());
            removedLanguagesId.removeAll(newList);
            System.out.println("Removed languages: " + removedLanguagesId);

//            removeTranslation(request.getRestaurantId(), removedLanguagesId);

            selectedLanguages.setLanguagesList(String.join(",", request.getLanguagesArray()));
            selectedLanguages.setUpdatedAt(Instant.now().getEpochSecond());
            selectedLanguagesRepository.save(selectedLanguages);
        } else {
            request.setLanguagesList(String.join(",", request.getLanguagesArray()));
            request.setCreatedAt(Instant.now().getEpochSecond());
            request.setUpdatedAt(Instant.now().getEpochSecond());
            selectedLanguagesRepository.save(request);
        }
    }

    public List<Language> getLanguagesByRestaurantId(String restaurantId, String acceptLanguage) {
        String finalAcceptLanguage = CheckHeader.checkHeaderLanguage(acceptLanguage);
        restaurantRepository.findByRestaurantId(restaurantId).orElseThrow(() -> ExceptionTools.notFoundException(".restaurantNotFound", finalAcceptLanguage, restaurantId));
        List<Language> languageList = languageRepository.findAll();
        Optional<SelectedLanguages> selectedLanguages = selectedLanguagesRepository.findByRestaurantId(restaurantId);
        if (selectedLanguages.isPresent()) {
            String containsLanguages = selectedLanguages.get().getLanguagesList();
            languageList.removeIf(language -> !containsLanguages.contains(language.getLanguageId()));
        } else return null;
        return languageList;
    }
}