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
    private final LoggingService loggingService;

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
            allergens.sort(Comparator.comparingInt(menu -> {
                int index = MenuIdsInOrder.indexOf(menu.getAllergenId());
                return index != -1 ? index : Integer.MAX_VALUE;
            }));
        }

        List<AllergenResponse> responseList = allergens.stream()
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
                            .translationId(allergen.getTranslationId())
                            .content(content.get())
                            .build();
                })
                .collect(Collectors.toList());

        loggingService.log(LogLevel.INFO, String.format("getAllMenusByRestaurantId %s %s %d", restaurantId, Message.FIND_COUNT.getMessage(), responseList.size()));
        return responseList;
    }

    public AllergenResponse getAllergenById(String allergenId, String acceptLanguage) throws JsonProcessingException {
        String finalAcceptLanguage = CheckHeader.checkHeaderLanguage(acceptLanguage);

        Allergen allergen = allergenRepository.findByAllergenId(allergenId).orElseThrow(() -> ExceptionTools.notFoundException(".allergenNotFound", finalAcceptLanguage, allergenId));
        Translation translation = translationRepository.findAllByTranslationId(allergen.getTranslationId())
                .orElseThrow(() -> ExceptionTools.notFoundException(".translationNotFound", finalAcceptLanguage, allergenId));

        ObjectMapper objectMapper = new ObjectMapper();
        Content<OnlyName> content = objectMapper.readValue(translation.getContent(), new TypeReference<>() {
        });

        loggingService.log(LogLevel.INFO, String.format("getAllergenById %s", allergenId));
        return AllergenResponse.builder()
                .allergenId(allergen.getAllergenId())
                .translationId(allergen.getTranslationId())
                .content(content)
                .build();
    }

    public AllergenResponseId createAllergen(Allergen request, String acceptLanguage) throws JsonProcessingException {
        selectedLanguage = CheckHeader.checkHeaderLanguage(acceptLanguage);
        Map<String, String> fieldErrors = new ResponseErrorMap<>();
        validateRequest(request, fieldErrors, true);
        if (fieldErrors.size() > 0)
            throw new BadFieldsResponse(HttpStatus.BAD_REQUEST, fieldErrors);
        request.setCreatedAt(Instant.now().getEpochSecond());
        request.setUpdatedAt(Instant.now().getEpochSecond());
        allergenRepository.save(request);

        {
            CreateTranslation<OnlyName> createTranslation = new CreateTranslation<>(translationRepository);
            MenuTranslationEntry entry = new MenuTranslationEntry(request.getName());
            Translation translation = createTranslation
                    .create(request.getAllergenId(), "allergen", request.getLanguage(), entry);
            request.setTranslationId(translation.getTranslationId());
            allergenRepository.save(request);
        }

        loggingService.log(LogLevel.INFO, String.format("addAllergen %s UUID=%s %s", request.getName(), request.getAllergenId(), Message.CREATE.getMessage()));
        return AllergenResponseId.builder().allergenId(request.getAllergenId()).build();
    }

    public void patchAllergenById(String allergenId, Allergen request, String acceptLanguage) throws JsonProcessingException {
        String finalAcceptLanguage = CheckHeader.checkHeaderLanguage(acceptLanguage);
        Map<String, String> fieldErrors = new ResponseErrorMap<>();

        Allergen allergen = allergenRepository.findByAllergenId(allergenId).orElseThrow(() -> ExceptionTools.notFoundException(".allergenNotFound", finalAcceptLanguage, allergenId));
        Translation translation = translationRepository.findAllByTranslationId(allergen.getTranslationId())
                .orElseThrow(() -> ExceptionTools.notFoundException(".translationNotFound", finalAcceptLanguage, allergenId));

        validateRequest(request, fieldErrors, false);
        allergen.setName(request.getName());
        updateTranslation(allergen, request.getLanguage(), translation);

        allergen.setUpdatedAt(Instant.now().getEpochSecond());
        allergenRepository.save(allergen);
        loggingService.log(LogLevel.INFO, String.format("patchAllergenById %s %s", allergenId, Message.UPDATE.getMessage()));
    }

    private void validateRequest(Allergen allergen, Map<String, String> fieldErrors, boolean required) {
        fieldErrors.put("language", Validator.validateLanguage(allergen.getLanguage(), selectedLanguage));

        if (required) {
            if ("restaurant".equals(allergen.getReferenceType())) {
                var restaurantInfo = restaurantRepository.findByRestaurantId(allergen.getReferenceId());

                if (restaurantInfo.isEmpty()) {
                    fieldErrors.put("reference_id", String.format(translation.getString(selectedLanguage + ".badReferenceId"), allergen.getReferenceId()));
                }
            } else {
                fieldErrors.put("reference_type", translation.getString(selectedLanguage + ".badReferenceType"));
            }
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
        loggingService.log(LogLevel.INFO, String.format("deleteAllergenById %s NAME=%s %s", allergenId, allergen.getName(), Message.DELETE.getMessage()));
    }
}