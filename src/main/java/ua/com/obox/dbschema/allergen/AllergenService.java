package ua.com.obox.dbschema.allergen;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import ua.com.obox.dbschema.restaurant.RestaurantRepository;
import ua.com.obox.dbschema.tools.FieldUpdateFunction;
import ua.com.obox.dbschema.tools.Validator;
import ua.com.obox.dbschema.tools.exception.Message;
import ua.com.obox.dbschema.tools.logging.LogLevel;
import ua.com.obox.dbschema.tools.logging.LoggingService;
import ua.com.obox.dbschema.tools.response.BadFieldsResponse;
import ua.com.obox.dbschema.tools.response.ResponseErrorMap;
import ua.com.obox.dbschema.tools.services.UpdateServiceHelper;
import ua.com.obox.dbschema.tools.translation.CheckHeader;
import ua.com.obox.dbschema.translation.Translation;
import ua.com.obox.dbschema.translation.TranslationRepository;
import ua.com.obox.dbschema.translation.assistant.CreateTranslation;
import ua.com.obox.dbschema.translation.assistant.ExistEntity;
import ua.com.obox.dbschema.translation.assistant.ExistName;
import ua.com.obox.dbschema.translation.responsebody.MenuTranslationEntry;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.ResourceBundle;

@Service
@RequiredArgsConstructor
public class AllergenService {
    private final RestaurantRepository restaurantRepository;
    private final AllergenRepository allergenRepository;
    private final TranslationRepository translationRepository;
    private final UpdateServiceHelper serviceHelper;
    private static final ResourceBundle translation = ResourceBundle.getBundle("translation.messages");
    private final LoggingService loggingService;


    private String selectedLanguage = "en-US";

    public void addAllergen(Allergen request, String acceptLanguage) throws JsonProcessingException {
        selectedLanguage = CheckHeader.checkHeaderLanguage(acceptLanguage);
        Map<String, String> fieldErrors = new ResponseErrorMap<>();
        validateRequest(request, fieldErrors, true);
        if (fieldErrors.size() > 0)
            throw new BadFieldsResponse(HttpStatus.BAD_REQUEST, fieldErrors);
        request.setCreatedAt(Instant.now().getEpochSecond());
        request.setUpdatedAt(Instant.now().getEpochSecond());
        allergenRepository.save(request);

        {
            CreateTranslation<ExistName> createTranslation = new CreateTranslation<>(translationRepository);
            MenuTranslationEntry entry = new MenuTranslationEntry(request.getName());
            Translation translation = createTranslation
                    .create(request.getAllergenId(), "allergen", request.getLanguage(), entry);
            request.setTranslationId(translation.getTranslationId());
            allergenRepository.save(request);
        }

        loggingService.log(LogLevel.INFO, String.format("addAllergen %s UUID=%s %s", request.getName(), request.getAllergenId(), Message.CREATE.getMessage()));
    }

    private void validateRequest(Allergen allergen, Map<String, String> fieldErrors, boolean required) {
        fieldErrors.put("language", Validator.validateLanguage(allergen.getLanguage(), selectedLanguage));

        if ("restaurant".equals(allergen.getReferenceType())) {
            var restaurantInfo = restaurantRepository.findByRestaurantId(allergen.getReferenceId());

            if (restaurantInfo.isEmpty()) {
                fieldErrors.put("reference_id", String.format(translation.getString(selectedLanguage + ".badReferenceId"), allergen.getReferenceId()));
            }
        } else {
            fieldErrors.put("reference_type", translation.getString(selectedLanguage + ".badReferenceType"));
        }

        updateField(allergen.getName(), required, allergen, fieldErrors, "name",
                (name) -> serviceHelper.updateNameField(allergen::setName, name, selectedLanguage), selectedLanguage);

        if (fieldErrors.size() > 0)
            throw new BadFieldsResponse(HttpStatus.BAD_REQUEST, fieldErrors);
    }

    private <T> void updateField(T value, boolean required, Allergen allergen, Map<String, String> fieldErrors, String fieldName, FieldUpdateFunction<T> updateFunction, String finalAcceptLanguage) {
        if (value != null || required) {
            if (Objects.equals(fieldName, "name") && allergen.getReferenceId() != null) {
                List<Allergen> sameParent = allergenRepository.findAllByReferenceId(allergen.getReferenceId());
                sameParent.remove(allergen);
                ExistEntity<ExistName> existEntity = new ExistEntity<>(translationRepository);
                existEntity.checkExistEntity(Validator.removeExtraSpaces((String) value), sameParent, finalAcceptLanguage, fieldErrors);
            }
            String error = updateFunction.updateField(value);
            if (error != null) {
                fieldErrors.put(fieldName, error);
            }
        }
    }
}
