package ua.com.obox.dbschema.language;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import ua.com.obox.dbschema.restaurant.RestaurantRepository;
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
    private final SelectedLanguageRepository selectedLanguageRepository;
    private final RestaurantRepository restaurantRepository;
    private static final ResourceBundle translation = ResourceBundle.getBundle("translation.messages");

    public List<LanguageResponse> getAllLanguages() {
        List<Language> languageList;
        List<LanguageResponse> responses = new ArrayList<>();
        languageList = languageRepository.findAll();
        if (languageList.size() == 0) {
            languageList.add(Language.builder().label("English").name("en-US").createdAt(Instant.now().getEpochSecond()).updatedAt(Instant.now().getEpochSecond()).build());
            languageList.add(Language.builder().label("Українська").name("uk-UA").createdAt(Instant.now().getEpochSecond()).updatedAt(Instant.now().getEpochSecond()).build());
            languageList.add(Language.builder().label("Español").name("es-ES").createdAt(Instant.now().getEpochSecond()).updatedAt(Instant.now().getEpochSecond()).build());
            languageList.add(Language.builder().label("Français").name("fr-FR").createdAt(Instant.now().getEpochSecond()).updatedAt(Instant.now().getEpochSecond()).build());
            languageList.add(Language.builder().label("Deutsch").name("de-DE").createdAt(Instant.now().getEpochSecond()).updatedAt(Instant.now().getEpochSecond()).build());
            languageList.add(Language.builder().label("Italiano").name("it-IT").createdAt(Instant.now().getEpochSecond()).updatedAt(Instant.now().getEpochSecond()).build());
            languageList.add(Language.builder().label("Português").name("pt-PT").createdAt(Instant.now().getEpochSecond()).updatedAt(Instant.now().getEpochSecond()).build());
        }

        for (Language language : languageList) {
            responses.add(LanguageResponse.builder().languageId(language.getLanguageId()).name(language.getName()).label(language.getLabel()).build());
        }

        languageRepository.saveAll(languageList);
        return responses;
    }

    public void addLanguageForRestaurant(SelectedLanguage request, String acceptLanguage) {
        String finalAcceptLanguage = CheckHeader.checkHeaderLanguage(acceptLanguage);
        restaurantRepository.findByRestaurantId(request.getRestaurantId()).orElseThrow(() -> ExceptionTools.notFoundException(".restaurantNotFound", finalAcceptLanguage, request.getRestaurantId()));
        languageRepository.findByLanguageId(request.getLanguageId()).orElseThrow(() -> ExceptionTools.notFoundException(".languageNotFound", finalAcceptLanguage, request.getLanguageId()));

        Map<String, String> fieldErrors = new ResponseErrorMap<>();
        Optional<SelectedLanguage> existLanguage = selectedLanguageRepository.findByLanguageIdAndRestaurantId(request.getLanguageId(), request.getRestaurantId());

        if (existLanguage.isPresent()) {
            fieldErrors.put("language_id", (String.format(translation.getString(finalAcceptLanguage + ".languageExists"), request.getLanguageId())));
        }

        if (fieldErrors.size() > 0)
            throw new BadFieldsResponse(HttpStatus.BAD_REQUEST, fieldErrors);

        SelectedLanguage selectedLanguage = SelectedLanguage.builder()
                .languageId(request.getLanguageId())
                .restaurantId(request.getRestaurantId())
                .createdAt(Instant.now().getEpochSecond())
                .updatedAt(Instant.now().getEpochSecond())
                .build();

        selectedLanguageRepository.save(selectedLanguage);
    }

    public List<LanguageResponse> getLanguagesByRestaurantId(String restaurantId, String acceptLanguage) {
        String finalAcceptLanguage = CheckHeader.checkHeaderLanguage(acceptLanguage);
        restaurantRepository.findByRestaurantId(restaurantId).orElseThrow(() -> ExceptionTools.notFoundException(".restaurantNotFound", finalAcceptLanguage, restaurantId));

        List<SelectedLanguage> selectedLanguages = selectedLanguageRepository.findByRestaurantId(restaurantId);
        List<LanguageResponse> response = new ArrayList<>();

        if (selectedLanguages.size() == 0)
            return response;

        for (SelectedLanguage selected : selectedLanguages) {
            Optional<Language> language = languageRepository.findByLanguageId(selected.getLanguageId());
            language.ifPresent(value -> response.add(LanguageResponse.builder()
                    .languageId(value.getLanguageId())
                    .name(value.getName())
                    .label(value.getLabel())
                    .build()));
        }

        return response;
    }

    public void removeLanguageFromRestaurant(String restaurantId, String languageId, String acceptLanguage) {
        String finalAcceptLanguage = CheckHeader.checkHeaderLanguage(acceptLanguage);
        SelectedLanguage selectedLanguage = selectedLanguageRepository.findByLanguageIdAndRestaurantId(languageId, restaurantId).orElseThrow(() -> ExceptionTools.notFoundException(".languageNotFound", finalAcceptLanguage, languageId));
        selectedLanguageRepository.delete(selectedLanguage);
    }
}