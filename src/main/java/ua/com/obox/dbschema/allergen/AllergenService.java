package ua.com.obox.dbschema.allergen;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import ua.com.obox.dbschema.restaurant.RestaurantRepository;
import ua.com.obox.dbschema.sorting.EntityOrder;
import ua.com.obox.dbschema.sorting.EntityOrderRepository;
import ua.com.obox.dbschema.tools.FieldUpdateFunction;
import ua.com.obox.dbschema.tools.Validator;
import ua.com.obox.dbschema.tools.exception.ExceptionTools;
import ua.com.obox.dbschema.tools.response.BadFieldsResponse;
import ua.com.obox.dbschema.tools.response.ResponseErrorMap;
import ua.com.obox.dbschema.tools.services.UpdateServiceHelper;
import ua.com.obox.dbschema.tools.translation.CheckHeader;
import ua.com.obox.dbschema.translation.Translation;
import ua.com.obox.dbschema.translation.TranslationRepository;
import ua.com.obox.dbschema.translation.assistant.CreateTranslation;
import ua.com.obox.dbschema.translation.assistant.ExistEntity;
import ua.com.obox.dbschema.translation.assistant.OnlyName;
import ua.com.obox.dbschema.translation.responsebody.Content;
import ua.com.obox.dbschema.translation.responsebody.MenuTranslationEntry;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AllergenService {
    private final RestaurantRepository restaurantRepository;
    private final AllergenRepository allergenRepository;
    private final TranslationRepository translationRepository;
    private final EntityOrderRepository entityOrderRepository;
    private final UpdateServiceHelper serviceHelper;
    private static final ResourceBundle translation = ResourceBundle.getBundle("translation.messages");
    private String selectedLanguage = "en-US";

    public List<AllergenResponse> getAllAllergensByRestaurantId(String restaurantId, String acceptLanguage) {
        selectedLanguage = CheckHeader.checkHeaderLanguage(acceptLanguage);
        ObjectMapper objectMapper = new ObjectMapper();
        AtomicReference<Content<OnlyName>> content = new AtomicReference<>();
        AtomicReference<Translation> translation = new AtomicReference<>();

        restaurantRepository.findByRestaurantId(restaurantId).orElseThrow(() -> ExceptionTools.notFoundException(".restaurantNotFound", selectedLanguage, restaurantId));

        List<Allergen> allergens = allergenRepository.findAllByReferenceIdOrderByCreatedAtDesc(restaurantId);

        // for sorting results
        EntityOrder sortingExist = entityOrderRepository.findByReferenceIdAndReferenceType(restaurantId, "allergens").orElse(null);
        if (sortingExist != null) {
            List<String> MenuIdsInOrder = Arrays.stream(sortingExist.getSortedList().split(",")).toList();
            allergens.sort(Comparator.comparingInt(allergen -> {
                int index = MenuIdsInOrder.indexOf(allergen.getAllergenId());
                return index != -1 ? index : Integer.MAX_VALUE;
            }));
        }

        return allergens.stream()
                .map(allergen -> {
                    try {
                        translation.set(translationRepository.findAllByTranslationId(allergen.getTranslationId()).orElseThrow(() ->
                                ExceptionTools.notFoundException(".translationNotFound", selectedLanguage, allergen.getAllergenId())));
                        content.set(objectMapper.readValue(translation.get().getContent(), new TypeReference<>() {
                        }));
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }

                    return AllergenResponse.builder()
                            .allergenId(allergen.getAllergenId())
                            .originalLanguage(allergen.getOriginalLanguage())
                            .translationId(allergen.getTranslationId())
                            .colorHex(allergen.getColorHex())
                            .emoji(allergen.getEmoji())
                            .content(content.get())
                            .build();
                })
                .collect(Collectors.toList());
    }

    public AllergenResponse getAllergenById(String allergenId, String acceptLanguage) throws JsonProcessingException {
        String finalAcceptLanguage = CheckHeader.checkHeaderLanguage(acceptLanguage);

        Allergen allergen = allergenRepository.findByAllergenId(allergenId).orElseThrow(() -> ExceptionTools.notFoundException(".allergenNotFound", finalAcceptLanguage, allergenId));
        Translation translation = translationRepository.findAllByTranslationId(allergen.getTranslationId())
                .orElseThrow(() -> ExceptionTools.notFoundException(".translationNotFound", finalAcceptLanguage, allergenId));

        ObjectMapper objectMapper = new ObjectMapper();
        Content<OnlyName> content = objectMapper.readValue(translation.getContent(), new TypeReference<>() {
        });

        return AllergenResponse.builder()
                .allergenId(allergen.getAllergenId())
                .originalLanguage(allergen.getOriginalLanguage())
                .translationId(allergen.getTranslationId())
                .colorHex(allergen.getColorHex())
                .emoji(allergen.getEmoji())
                .content(content)
                .build();
    }

    public AllergenResponseId createAllergen(Allergen request, String acceptLanguage) throws JsonProcessingException {
        selectedLanguage = CheckHeader.checkHeaderLanguage(acceptLanguage);
        Map<String, String> fieldErrors = new ResponseErrorMap<>();
        Allergen allergen = Allergen.builder().build();
        validateRequest(request, allergen, fieldErrors, true);
        if (fieldErrors.size() > 0)
            throw new BadFieldsResponse(HttpStatus.BAD_REQUEST, fieldErrors);
        allergen.setOriginalLanguage(request.getLanguage());
        allergen.setCreatedAt(Instant.now().getEpochSecond());
        allergen.setUpdatedAt(Instant.now().getEpochSecond());
        allergenRepository.save(allergen);

        {
            CreateTranslation<OnlyName> createTranslation = new CreateTranslation<>(translationRepository);
            MenuTranslationEntry entry = new MenuTranslationEntry(allergen.getName());
            Translation translation = createTranslation
                    .create(allergen.getAllergenId(), "allergen", request.getLanguage(), entry);
            allergen.setTranslationId(translation.getTranslationId());
            allergenRepository.save(allergen);
        }

        return AllergenResponseId.builder().allergenId(allergen.getAllergenId()).build();
    }

    public void patchAllergenById(String allergenId, Allergen request, String acceptLanguage) throws JsonProcessingException {
        selectedLanguage = CheckHeader.checkHeaderLanguage(acceptLanguage);
        Map<String, String> fieldErrors = new ResponseErrorMap<>();

        Allergen allergen = allergenRepository.findByAllergenId(allergenId).orElseThrow(() -> ExceptionTools.notFoundException(".allergenNotFound", selectedLanguage, allergenId));
        Translation translation = translationRepository.findAllByTranslationId(allergen.getTranslationId())
                .orElseThrow(() -> ExceptionTools.notFoundException(".translationNotFound", selectedLanguage, allergenId));
        validateRequest(request, allergen, fieldErrors, false);
//        allergen.setName(request.getName());
        updateTranslation(allergen, request.getLanguage(), translation);
        allergen.setUpdatedAt(Instant.now().getEpochSecond());
        allergenRepository.save(allergen);
    }

    private void validateRequest(Allergen request, Allergen allergen, Map<String, String> fieldErrors, boolean required) {
        fieldErrors.put("language", Validator.validateLanguage(request.getLanguage(), selectedLanguage));

        if (required) {
            if ("restaurant".equals(request.getReferenceType())) {
                var restaurantInfo = restaurantRepository.findByRestaurantId(request.getReferenceId());
                allergen.setReferenceType(request.getReferenceType());
                if (restaurantInfo.isEmpty()) {
                    fieldErrors.put("reference_id", String.format(translation.getString(selectedLanguage + ".badReferenceId"), allergen.getReferenceId()));
                }
                allergen.setReferenceId(request.getReferenceId());
            } else {
                fieldErrors.put("reference_type", translation.getString(selectedLanguage + ".badReferenceType"));
            }
        }

        updateField(request.getName(), required, request, fieldErrors, "name",
                (name) -> serviceHelper.updateNameField(allergen::setName, name, selectedLanguage), selectedLanguage);

        updateField(request.getColorHex(), required, request, fieldErrors, "color_hex",
                (color) -> serviceHelper.updateColorHex(allergen::setColorHex, color, selectedLanguage), selectedLanguage);

        updateField(request.getEmoji(), false, request, fieldErrors, "emoji",
                (emoji) -> serviceHelper.updateEmoji(allergen::setEmoji, emoji, selectedLanguage), selectedLanguage);

        if (fieldErrors.size() > 0)
            throw new BadFieldsResponse(HttpStatus.BAD_REQUEST, fieldErrors);
    }

    private <T> void updateField(T value, boolean required, Allergen allergen, Map<String, String> fieldErrors, String fieldName, FieldUpdateFunction<T> updateFunction, String finalAcceptLanguage) {
        if (value != null || required) {
            if (Objects.equals(fieldName, "name") && allergen.getReferenceId() != null) {
                List<Allergen> sameParent = allergenRepository.findAllByReferenceId(allergen.getReferenceId());
                sameParent.remove(allergen);
                ExistEntity<OnlyName> existEntity = new ExistEntity<>(translationRepository);
                existEntity.checkExistEntity(Validator.removeExtraSpaces((String) value), sameParent, finalAcceptLanguage, fieldErrors);
            }
            String error = updateFunction.updateField(value);
            if (error != null) {
                fieldErrors.put(fieldName, error);
            }
        }
    }

    private void updateTranslation(Allergen allergen, String language, Translation translation) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        TypeReference<Content<OnlyName>> typeReference = new TypeReference<>() {
        };
        Content<OnlyName> content = objectMapper.readValue(translation.getContent(), typeReference);
        Map<String, OnlyName> languagesMap = content.getContent();
        if (languagesMap.get(language) != null) {
            if (allergen.getName() == null)
                allergen.setName(languagesMap.get(language).getName());
        }
        languagesMap.put(language, new OnlyName(allergen.getName()));
        translation.setContent(objectMapper.writeValueAsString(content));
        translation.setUpdatedAt(Instant.now().getEpochSecond());
    }

    public void deleteAllergenById(String allergenId, String acceptLanguage) {
        String finalAcceptLanguage = CheckHeader.checkHeaderLanguage(acceptLanguage);
        Allergen allergen = allergenRepository.findByAllergenId(allergenId).orElseThrow(() -> ExceptionTools.notFoundException(".allergenNotFound", finalAcceptLanguage, allergenId));
        allergenRepository.delete(allergen);
    }
}
