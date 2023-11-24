package ua.com.obox.dbschema.search;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ua.com.obox.dbschema.allergen.AllergenRepository;
import ua.com.obox.dbschema.attachment.AttachmentRepository;
import ua.com.obox.dbschema.dish.Dish;
import ua.com.obox.dbschema.dish.DishRepository;
import ua.com.obox.dbschema.dish.DishResponse;
import ua.com.obox.dbschema.mark.MarkRepository;
import ua.com.obox.dbschema.restaurant.RestaurantRepository;
import ua.com.obox.dbschema.tools.attachment.AttachmentTools;
import ua.com.obox.dbschema.tools.exception.ExceptionTools;
import ua.com.obox.dbschema.tools.exception.Message;
import ua.com.obox.dbschema.tools.logging.LogLevel;
import ua.com.obox.dbschema.tools.logging.LoggingService;
import ua.com.obox.dbschema.tools.translation.CheckHeader;
import ua.com.obox.dbschema.translation.Translation;
import ua.com.obox.dbschema.translation.TranslationRepository;
import ua.com.obox.dbschema.translation.responsebody.CategoryTranslationEntry;
import ua.com.obox.dbschema.translation.responsebody.Content;

import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SearchService {
    private final RestaurantRepository restaurantRepository;
    private final AllergenRepository allergenRepository;
    private final MarkRepository markRepository;
    private final DishRepository dishRepository;
    private final TranslationRepository translationRepository;
    private final AttachmentRepository attachmentRepository;
    private static final ResourceBundle translation = ResourceBundle.getBundle("translation.messages");
    private final LoggingService loggingService;
    private String selectedLanguage = "en-US";

    @Value("${application.image-dns}")
    private String attachmentsDns;

    public List<DishResponse> getAllDishesByAllergenId(String allergenId, String acceptLanguage) {
        selectedLanguage = CheckHeader.checkHeaderLanguage(acceptLanguage);
        String finalAcceptLanguage = CheckHeader.checkHeaderLanguage(acceptLanguage);
        ObjectMapper objectMapper = new ObjectMapper();
        AtomicReference<Content<CategoryTranslationEntry>> content = new AtomicReference<>();
        AtomicReference<Translation> translation = new AtomicReference<>();

        allergenRepository.findByAllergenId(allergenId).orElseThrow(() -> ExceptionTools.notFoundException(".allergenNotFound", finalAcceptLanguage, allergenId));

        List<Dish> dishes = dishRepository.findAllByAllergensContainingOrderByCreatedAtDesc(allergenId);

        List<DishResponse> responseList = dishes.stream()
                .map(dish -> {
                    try {
                        translation.set(translationRepository.findAllByTranslationId(dish.getTranslationId()).orElseThrow(() ->
                                ExceptionTools.notFoundException(".translationNotFound", finalAcceptLanguage, dish.getDishId())));
                        content.set(objectMapper.readValue(translation.get().getContent(), new TypeReference<>() {
                        }));
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }

                    return DishResponse.builder()
                            .categoryId(dish.getCategory().getCategoryId())
                            .dishId(dish.getDishId())
                            .translationId(dish.getTranslationId())
                            .price(dish.getPrice())
                            .specialPrice(dish.getSpecialPrice())
                            .cookingTime(dish.getCookingTime())
                            .calories(dish.getCalories())
                            .weight(dish.getWeight())
                            .weightUnit(dish.getWeightUnit())
                            .inStock(dish.getInStock())
                            .state(dish.getState())
                            .allergens(dish.getAllergens() == null ? null : Arrays.stream(dish.getAllergens().split(",")).toList())
                            .marks(dish.getMarks() == null ? null : Arrays.stream(dish.getMarks().split(",")).toList())
                            .image(AttachmentTools.getURL(dish, attachmentRepository, attachmentsDns))
                            .content(content.get())
                            .build();
                })
                .collect(Collectors.toList());

        loggingService.log(LogLevel.INFO, String.format("getAllDishesByAllergenId %s %s %d", allergenId, Message.FIND_COUNT.getMessage(), responseList.size()));
        return responseList;
    }


    public List<DishResponse> getAllMarksByAllergenId(String markId, String acceptLanguage) {
        selectedLanguage = CheckHeader.checkHeaderLanguage(acceptLanguage);
        String finalAcceptLanguage = CheckHeader.checkHeaderLanguage(acceptLanguage);
        ObjectMapper objectMapper = new ObjectMapper();
        AtomicReference<Content<CategoryTranslationEntry>> content = new AtomicReference<>();
        AtomicReference<Translation> translation = new AtomicReference<>();

        markRepository.findByMarkId(markId).orElseThrow(() -> ExceptionTools.notFoundException(".markNotFound", finalAcceptLanguage, markId));

        List<Dish> dishes = dishRepository.findAllByMarksContainingOrderByCreatedAtDesc(markId);

        List<DishResponse> responseList = dishes.stream()
                .map(dish -> {
                    try {
                        translation.set(translationRepository.findAllByTranslationId(dish.getTranslationId()).orElseThrow(() ->
                                ExceptionTools.notFoundException(".translationNotFound", finalAcceptLanguage, dish.getDishId())));
                        content.set(objectMapper.readValue(translation.get().getContent(), new TypeReference<>() {
                        }));
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }

                    return DishResponse.builder()
                            .categoryId(dish.getCategory().getCategoryId())
                            .dishId(dish.getDishId())
                            .translationId(dish.getTranslationId())
                            .price(dish.getPrice())
                            .specialPrice(dish.getSpecialPrice())
                            .cookingTime(dish.getCookingTime())
                            .calories(dish.getCalories())
                            .weight(dish.getWeight())
                            .weightUnit(dish.getWeightUnit())
                            .inStock(dish.getInStock())
                            .state(dish.getState())
                            .allergens(dish.getAllergens() == null ? null : Arrays.stream(dish.getAllergens().split(",")).toList())
                            .marks(dish.getMarks() == null ? null : Arrays.stream(dish.getMarks().split(",")).toList())
                            .image(AttachmentTools.getURL(dish, attachmentRepository, attachmentsDns))
                            .content(content.get())
                            .build();
                })
                .collect(Collectors.toList());

        loggingService.log(LogLevel.INFO, String.format("getAllMarksByAllergenId %s %s %d", markId, Message.FIND_COUNT.getMessage(), responseList.size()));
        return responseList;
    }
}
