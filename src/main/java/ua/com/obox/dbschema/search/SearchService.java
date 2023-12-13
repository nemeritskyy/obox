package ua.com.obox.dbschema.search;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import ua.com.obox.dbschema.allergen.AllergenRepository;
import ua.com.obox.dbschema.attachment.AttachmentRepository;
import ua.com.obox.dbschema.category.Category;
import ua.com.obox.dbschema.category.CategoryRepository;
import ua.com.obox.dbschema.category.CategoryResponse;
import ua.com.obox.dbschema.dish.Dish;
import ua.com.obox.dbschema.dish.DishRepository;
import ua.com.obox.dbschema.dish.DishResponse;
import ua.com.obox.dbschema.mark.MarkRepository;
import ua.com.obox.dbschema.tools.attachment.AttachmentTools;
import ua.com.obox.dbschema.tools.configuration.ValidationConfiguration;
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
import ua.com.obox.dbschema.translation.assistant.GetTranslation;
import ua.com.obox.dbschema.translation.responsebody.CategoryTranslationEntry;
import ua.com.obox.dbschema.translation.responsebody.Content;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SearchService {
    private final AllergenRepository allergenRepository;
    private final MarkRepository markRepository;
    private final CategoryRepository categoryRepository;
    private final DishRepository dishRepository;
    private final TranslationRepository translationRepository;
    private final AttachmentRepository attachmentRepository;
    private final UpdateServiceHelper serviceHelper;
    private static final ResourceBundle translation = ResourceBundle.getBundle("translation.messages");
    private final LoggingService loggingService;
    private final GetTranslation getTranslation;
    private String selectedLanguage = "en-US";

    @Value("${application.image-dns}")
    private String attachmentsDns;

    public List<CategoryResponse> getAllDishesByAllergenId(String allergenId, String acceptLanguage) {
        selectedLanguage = CheckHeader.checkHeaderLanguage(acceptLanguage);
        String finalAcceptLanguage = CheckHeader.checkHeaderLanguage(acceptLanguage);
        ObjectMapper objectMapper = new ObjectMapper();
        AtomicReference<Content<CategoryTranslationEntry>> content = new AtomicReference<>();
        AtomicReference<Translation> translation = new AtomicReference<>();

        allergenRepository.findByAllergenId(allergenId).orElseThrow(() -> ExceptionTools.notFoundException(".allergenNotFound", finalAcceptLanguage, allergenId));

        List<Dish> dishes = dishRepository.findAllByAllergensContainingOrderByCreatedAtDesc(allergenId);

        Set<Category> categories = new HashSet<>();
        for (Dish dish : dishes) {
            categories.add(dish.getCategory());
        }

        List<CategoryResponse> categoryResponseList = categories.stream()
                .map(category -> {
                    try {
                        translation.set(translationRepository.findAllByTranslationId(category.getTranslationId()).orElseThrow(() ->
                                ExceptionTools.notFoundException(".translationNotFound", finalAcceptLanguage, category.getCategoryId())));
                        content.set(objectMapper.readValue(translation.get().getContent(), new TypeReference<>() {
                        }));
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }

                    CategoryResponse categoryResponse = CategoryResponse.builder()
                            .menuId(category.getMenu().getMenuId())
                            .categoryId(category.getCategoryId())
                            .translationId(category.getTranslationId())
                            .state(category.getState())
                            .content(content.get())
                            .build();

                    List<DishResponse> dishResponseList = category.getDishes().stream().filter((dishes::contains))
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
                            }).collect(Collectors.toList());

                    categoryResponse.setDishes(dishResponseList);
                    return categoryResponse;
                }).toList();

        loggingService.log(LogLevel.INFO, String.format("getAllDishesByAllergenId %s %s %d", allergenId, Message.FIND_COUNT.getMessage(), categoryResponseList.size()));
        return categoryResponseList;
    }


    public List<CategoryResponse> getAllMarksByAllergenId(String markId, String acceptLanguage) {
        selectedLanguage = CheckHeader.checkHeaderLanguage(acceptLanguage);
        String finalAcceptLanguage = CheckHeader.checkHeaderLanguage(acceptLanguage);
        ObjectMapper objectMapper = new ObjectMapper();
        AtomicReference<Content<CategoryTranslationEntry>> content = new AtomicReference<>();
        AtomicReference<Translation> translation = new AtomicReference<>();

        markRepository.findByMarkId(markId).orElseThrow(() -> ExceptionTools.notFoundException(".markNotFound", finalAcceptLanguage, markId));

        List<Dish> dishes = dishRepository.findAllByMarksContainingOrderByCreatedAtDesc(markId);

        Set<Category> categories = new HashSet<>();
        for (Dish dish : dishes) {
            categories.add(dish.getCategory());
        }

        List<CategoryResponse> categoryResponseList = categories.stream()
                .map(category -> {
                    try {
                        translation.set(translationRepository.findAllByTranslationId(category.getTranslationId()).orElseThrow(() ->
                                ExceptionTools.notFoundException(".translationNotFound", finalAcceptLanguage, category.getCategoryId())));
                        content.set(objectMapper.readValue(translation.get().getContent(), new TypeReference<>() {
                        }));
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }

                    CategoryResponse categoryResponse = CategoryResponse.builder()
                            .menuId(category.getMenu().getMenuId())
                            .categoryId(category.getCategoryId())
                            .translationId(category.getTranslationId())
                            .state(category.getState())
                            .content(content.get())
                            .build();

                    List<DishResponse> dishResponseList = category.getDishes().stream().filter((dishes::contains))
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
                            }).collect(Collectors.toList());

                    categoryResponse.setDishes(dishResponseList);
                    return categoryResponse;
                }).toList();

        loggingService.log(LogLevel.INFO, String.format("getAllMarksByAllergenId %s %s %d", markId, Message.FIND_COUNT.getMessage(), categoryResponseList.size()));
        return categoryResponseList;
    }

    public List<SearchResponse> searchAllByQuery(SearchQuery query, String errorLanguage) throws JsonProcessingException {
        String finalAcceptLanguage = CheckHeader.checkHeaderLanguage(errorLanguage);
        Map<String, String> fieldErrors = new ResponseErrorMap<>();
        fieldErrors.put("title", serviceHelper.updateNameField(query::setTitle, query.getTitle(), finalAcceptLanguage) != null ? translation.getString(finalAcceptLanguage + ".searchLimit") : null);
        if (fieldErrors.size() > 0)
            throw new BadFieldsResponse(HttpStatus.BAD_REQUEST, fieldErrors);
        ObjectMapper objectMapper = new ObjectMapper();
        String path1;
        String path2;
        Category categoryForPath;
        CategoryTranslationEntry categoryTranslationEntry;
        Content<CategoryTranslationEntry> categoryTranslationEntryContent;
        List<SearchResponse> searchResponses = new ArrayList<>();
        List<String> supportLanguages = ValidationConfiguration.SUPPORT_LANGUAGES;
        List<Translation> categoriesResults = translationRepository.findAllByReferenceTypeAndContentContainingIgnoreCase("category", query.getTitle());
        for (Translation category : categoriesResults) {
            categoryTranslationEntryContent = objectMapper.readValue(category.getContent(), new TypeReference<>() {
            });
            for (String entryLanguage : supportLanguages) {
                categoryTranslationEntry = categoryTranslationEntryContent.getContent().get(entryLanguage);
                if (categoryTranslationEntry != null && categoryTranslationEntry.getName() != null && categoryTranslationEntry.getName().toLowerCase().contains(query.getTitle().toLowerCase())) {
                    path1 = getTranslation.getParentNameByEntityId(categoryRepository.findByCategoryId(category.getReferenceId()).get().getMenu().getMenuId(), entryLanguage, errorLanguage);
                    searchResponses.add(new SearchResponse(category.getReferenceId(), category.getReferenceType(), entryLanguage, path1, categoryTranslationEntry.getName()));
                }

            }
        }
        List<Translation> dishesResults = translationRepository.findAllByReferenceTypeAndContentContainingIgnoreCase("dish", query.getTitle());
        for (Translation category : dishesResults) {
            categoryTranslationEntryContent = objectMapper.readValue(category.getContent(), new TypeReference<>() {
            });
            for (String entryLanguage : supportLanguages) {
                categoryTranslationEntry = categoryTranslationEntryContent.getContent().get(entryLanguage);
                if (categoryTranslationEntry != null && categoryTranslationEntry.getName() != null && categoryTranslationEntry.getName().toLowerCase().contains(query.getTitle().toLowerCase())) {
                    categoryForPath = dishRepository.findByDishId(category.getReferenceId()).get().getCategory();
                    path1 = getTranslation.getParentNameByEntityId(categoryForPath.getCategoryId(), entryLanguage, errorLanguage);
                    path2 = getTranslation.getParentNameByEntityId(categoryForPath.getMenu().getMenuId(), entryLanguage, errorLanguage);
                    searchResponses.add(new SearchResponse(category.getReferenceId(), category.getReferenceType(), entryLanguage, String.format("%s/%s", path2, path1), categoryTranslationEntry.getName()));
                }
            }
        }
        return searchResponses;
    }
}
