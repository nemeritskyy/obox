package ua.com.obox.dbschema.mark;

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
public class MarkService {
    private final RestaurantRepository restaurantRepository;
    private final MarkRepository markRepository;
    private final TranslationRepository translationRepository;
    private final EntityOrderRepository entityOrderRepository;
    private final UpdateServiceHelper serviceHelper;
    private static final ResourceBundle translation = ResourceBundle.getBundle("translation.messages");

    private String selectedLanguage = "en-US";

    public List<MarkResponse> getAllMarksByRestaurantId(String restaurantId, String acceptLanguage) {
        selectedLanguage = CheckHeader.checkHeaderLanguage(acceptLanguage);
        ObjectMapper objectMapper = new ObjectMapper();
        AtomicReference<Content<OnlyName>> content = new AtomicReference<>();
        AtomicReference<Translation> translation = new AtomicReference<>();

        restaurantRepository.findByRestaurantId(restaurantId).orElseThrow(() -> ExceptionTools.notFoundException(".restaurantNotFound", selectedLanguage, restaurantId));

        List<Mark> marks = markRepository.findAllByReferenceIdOrderByCreatedAtDesc(restaurantId);

        // for sorting results
        EntityOrder sortingExist = entityOrderRepository.findByReferenceIdAndReferenceType(restaurantId, "marks").orElse(null);
        if (sortingExist != null) {
            List<String> MenuIdsInOrder = Arrays.stream(sortingExist.getSortedList().split(",")).toList();
            marks.sort(Comparator.comparingInt(mark -> {
                int index = MenuIdsInOrder.indexOf(mark.getMarkId());
                return index != -1 ? index : Integer.MAX_VALUE;
            }));
        }

        return marks.stream()
                .map(mark -> {
                    try {
                        translation.set(translationRepository.findAllByTranslationId(mark.getTranslationId()).orElseThrow(() ->
                                ExceptionTools.notFoundException(".translationNotFound", selectedLanguage, mark.getMarkId())));
                        content.set(objectMapper.readValue(translation.get().getContent(), new TypeReference<>() {
                        }));
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }

                    return MarkResponse.builder()
                            .markId(mark.getMarkId())
                            .originalLanguage(mark.getOriginalLanguage())
                            .translationId(mark.getTranslationId())
                            .colorBackground(mark.getColorBackground())
                            .colorText(mark.getColorText())
                            .emoji(mark.getEmoji())
                            .content(content.get())
                            .build();
                })
                .collect(Collectors.toList());
    }

    public MarkResponse getMarkById(String markId, String acceptLanguage) throws JsonProcessingException {
        String finalAcceptLanguage = CheckHeader.checkHeaderLanguage(acceptLanguage);

        Mark mark = markRepository.findByMarkId(markId).orElseThrow(() -> ExceptionTools.notFoundException(".markNotFound", finalAcceptLanguage, markId));
        Translation translation = translationRepository.findAllByTranslationId(mark.getTranslationId())
                .orElseThrow(() -> ExceptionTools.notFoundException(".translationNotFound", finalAcceptLanguage, markId));

        ObjectMapper objectMapper = new ObjectMapper();
        Content<OnlyName> content = objectMapper.readValue(translation.getContent(), new TypeReference<>() {
        });

        return MarkResponse.builder()
                .markId(mark.getMarkId())
                .originalLanguage(mark.getOriginalLanguage())
                .translationId(mark.getTranslationId())
                .colorBackground(mark.getColorBackground())
                .colorText(mark.getColorText())
                .emoji(mark.getEmoji())
                .content(content)
                .build();
    }

    public MarkResponseId createMark(Mark request, String acceptLanguage) throws JsonProcessingException {
        selectedLanguage = CheckHeader.checkHeaderLanguage(acceptLanguage);
        Mark mark = Mark.builder().build();
        Map<String, String> fieldErrors = new ResponseErrorMap<>();
        validateRequest(request, mark, fieldErrors, true);
        if (fieldErrors.size() > 0)
            throw new BadFieldsResponse(HttpStatus.BAD_REQUEST, fieldErrors);
        mark.setOriginalLanguage(request.getLanguage());
        mark.setCreatedAt(Instant.now().getEpochSecond());
        mark.setUpdatedAt(Instant.now().getEpochSecond());
        markRepository.save(mark);

        {
            CreateTranslation<OnlyName> createTranslation = new CreateTranslation<>(translationRepository);
            MenuTranslationEntry entry = new MenuTranslationEntry(mark.getName());
            Translation translation = createTranslation
                    .create(mark.getMarkId(), "mark", request.getLanguage(), entry);
            mark.setTranslationId(translation.getTranslationId());
            markRepository.save(mark);
        }

        return MarkResponseId.builder().markId(mark.getMarkId()).build();
    }

    public void patchMarkById(String markId, Mark request, String acceptLanguage) throws JsonProcessingException {
        selectedLanguage = CheckHeader.checkHeaderLanguage(acceptLanguage);
        Map<String, String> fieldErrors = new ResponseErrorMap<>();

        Mark mark = markRepository.findByMarkId(markId).orElseThrow(() -> ExceptionTools.notFoundException(".markNotFound", selectedLanguage, markId));
        Translation translation = translationRepository.findAllByTranslationId(mark.getTranslationId())
                .orElseThrow(() -> ExceptionTools.notFoundException(".translationNotFound", selectedLanguage, markId));

        validateRequest(request, mark, fieldErrors, false);
        updateTranslation(mark, request.getLanguage(), translation);
        mark.setUpdatedAt(Instant.now().getEpochSecond());
        markRepository.save(mark);
    }

    private void validateRequest(Mark request, Mark mark, Map<String, String> fieldErrors, boolean required) {
        fieldErrors.put("language", Validator.validateLanguage(request.getLanguage(), selectedLanguage));

        if (required) {
            if ("restaurant".equals(request.getReferenceType())) {
                var restaurantInfo = restaurantRepository.findByRestaurantId(request.getReferenceId());
                mark.setReferenceType(request.getReferenceType());
                if (restaurantInfo.isEmpty()) {
                    fieldErrors.put("reference_id", String.format(translation.getString(selectedLanguage + ".badReferenceId"), mark.getReferenceId()));
                }
                mark.setReferenceId(request.getReferenceId());
            } else {
                fieldErrors.put("reference_type", translation.getString(selectedLanguage + ".badReferenceType"));
            }
        }

        updateField(request.getName(), required, mark, fieldErrors, "name",
                (name) -> serviceHelper.updateNameField(mark::setName, name, selectedLanguage), selectedLanguage);

        updateField(request.getColorBackground(), required, request, fieldErrors, "color_background",
                (color) -> serviceHelper.updateColor(mark::setColorBackground, color, selectedLanguage), selectedLanguage);

        updateField(request.getColorText(), required, request, fieldErrors, "color_text",
                (color) -> serviceHelper.updateColor(mark::setColorText, color, selectedLanguage), selectedLanguage);

        updateField(request.getEmoji(), false, request, fieldErrors, "emoji",
                (emoji) -> serviceHelper.updateEmoji(mark::setEmoji, emoji, selectedLanguage), selectedLanguage);

        if (fieldErrors.size() > 0)
            throw new BadFieldsResponse(HttpStatus.BAD_REQUEST, fieldErrors);
    }

    private <T> void updateField(T value, boolean required, Mark mark, Map<String, String> fieldErrors, String fieldName, FieldUpdateFunction<T> updateFunction, String finalAcceptLanguage) {
        if (value != null || required) {
            if (Objects.equals(fieldName, "name") && mark.getReferenceId() != null) {
                List<Mark> sameParent = markRepository.findAllByReferenceId(mark.getReferenceId());
                sameParent.remove(mark);
                ExistEntity<OnlyName> existEntity = new ExistEntity<>(translationRepository);
                existEntity.checkExistEntity(Validator.removeExtraSpaces((String) value), sameParent, finalAcceptLanguage, fieldErrors);
            }
            String error = updateFunction.updateField(value);
            if (error != null) {
                fieldErrors.put(fieldName, error);
            }
        }
    }

    private void updateTranslation(Mark mark, String language, Translation translation) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        TypeReference<Content<OnlyName>> typeReference = new TypeReference<>() {
        };
        Content<OnlyName> content = objectMapper.readValue(translation.getContent(), typeReference);
        Map<String, OnlyName> languagesMap = content.getContent();
        if (languagesMap.get(language) != null) {
            if (mark.getName() == null)
                mark.setName(languagesMap.get(language).getName());
        }
        languagesMap.put(language, new OnlyName(mark.getName()));
        translation.setContent(objectMapper.writeValueAsString(content));
        translation.setUpdatedAt(Instant.now().getEpochSecond());
    }

    public void deleteMarkById(String markId, String acceptLanguage) {
        String finalAcceptLanguage = CheckHeader.checkHeaderLanguage(acceptLanguage);
        Mark mark = markRepository.findByMarkId(markId).orElseThrow(() -> ExceptionTools.notFoundException(".markNotFound", finalAcceptLanguage, markId));
        markRepository.delete(mark);
    }
}
