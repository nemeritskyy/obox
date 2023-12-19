package ua.com.obox.dbschema.category;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import ua.com.obox.dbschema.attachment.AttachmentRepository;
import ua.com.obox.dbschema.menu.Menu;
import ua.com.obox.dbschema.menu.MenuRepository;
import ua.com.obox.dbschema.dish.Dish;
import ua.com.obox.dbschema.dish.DishRepository;
import ua.com.obox.dbschema.dish.DishResponse;
import ua.com.obox.dbschema.sorting.EntityOrder;
import ua.com.obox.dbschema.sorting.EntityOrderRepository;
import ua.com.obox.dbschema.tools.FieldUpdateFunction;
import ua.com.obox.dbschema.tools.Validator;
import ua.com.obox.dbschema.tools.attachment.AttachmentTools;
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
import ua.com.obox.dbschema.translation.responsebody.CategoryTranslationEntry;
import ua.com.obox.dbschema.translation.responsebody.Content;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {
    private final CategoryRepository categoryRepository;
    private final MenuRepository menuRepository;
    private final DishRepository dishRepository;
    private final EntityOrderRepository entityOrderRepository;
    private final TranslationRepository translationRepository;
    private final AttachmentRepository attachmentRepository;
    private final LoggingService loggingService;
    private final UpdateServiceHelper serviceHelper;
    private final ResourceBundle translationContent = ResourceBundle.getBundle("translation.messages");
    @Value("${application.image-dns}")
    private String attachmentsDns;

    public List<DishResponse> getAllDishesByCategoryId(String categoryId, String acceptLanguage) {
        String finalAcceptLanguage = CheckHeader.checkHeaderLanguage(acceptLanguage);
        ObjectMapper objectMapper = new ObjectMapper();
        AtomicReference<Content<CategoryTranslationEntry>> content = new AtomicReference<>();
        AtomicReference<Translation> translation = new AtomicReference<>();

        categoryRepository.findByCategoryId(categoryId).orElseThrow(() -> ExceptionTools.notFoundException(".categoryNotFound", finalAcceptLanguage, categoryId));

        List<Dish> dishes = dishRepository.findAllByCategory_CategoryIdOrderByCreatedAtDesc(categoryId);

        // for sorting results
        EntityOrder sortingExist = entityOrderRepository.findByReferenceIdAndReferenceType(categoryId, "category").orElse(null);
        if (sortingExist != null) {
            List<String> dishIdsInOrder = Arrays.stream(sortingExist.getSortedList().split(",")).toList();
            dishes.sort(Comparator.comparingInt(dish -> {
                int index = dishIdsInOrder.indexOf(dish.getDishId());
                return index != -1 ? index : Integer.MAX_VALUE;
            }));
        }

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

        loggingService.log(LogLevel.INFO, String.format("getAllDishesByCategoryId %s %s %d", categoryId, Message.FIND_COUNT.getMessage(), responseList.size()));
        return responseList;
    }

    public CategoryResponse getCategoryById(String categoryId, String acceptLanguage) throws JsonProcessingException {
        String finalAcceptLanguage = CheckHeader.checkHeaderLanguage(acceptLanguage);

        Category category = categoryRepository.findByCategoryId(categoryId).orElseThrow(() -> ExceptionTools.notFoundException(".categoryNotFound", finalAcceptLanguage, categoryId));
        Translation translation = translationRepository.findAllByTranslationId(category.getTranslationId())
                .orElseThrow(() -> ExceptionTools.notFoundException(".translationNotFound", finalAcceptLanguage, categoryId));

        ObjectMapper objectMapper = new ObjectMapper();
        Content<CategoryTranslationEntry> content = objectMapper.readValue(translation.getContent(), new TypeReference<>() {
        });

        loggingService.log(LogLevel.INFO, String.format("getCategoryById %s", categoryId));
        return CategoryResponse.builder()
                .menuId(category.getMenu().getMenuId())
                .categoryId(category.getCategoryId())
                .translationId(category.getTranslationId())
                .content(content)
                .state(category.getState())
                .build();
    }

    public CategoryResponseId createCategory(Category request, String acceptLanguage) throws JsonProcessingException {
        String finalAcceptLanguage = CheckHeader.checkHeaderLanguage(acceptLanguage);
        Map<String, String> fieldErrors = new ResponseErrorMap<>();

        Optional<Menu> menu = menuRepository.findByMenuId(request.getMenuId());
        if (menu.isEmpty())
            fieldErrors.put("menu_id", String.format(translationContent.getString(finalAcceptLanguage + ".menuNotFound"), request.getMenuId()));


        Category category = Category.builder()
                .menu(menu.orElse(null))
                .build();

        validateRequest(request, category, finalAcceptLanguage, fieldErrors, true);

        category.setCreatedAt(Instant.now().getEpochSecond());
        category.setUpdatedAt(Instant.now().getEpochSecond());
        categoryRepository.save(category);

        {
            CreateTranslation<CategoryTranslationEntry> createTranslation = new CreateTranslation<>(translationRepository);
            CategoryTranslationEntry entry = new CategoryTranslationEntry(category.getName(), category.getDescription());
            Translation translation = createTranslation
                    .create(category.getCategoryId(), "category", request.getLanguage(), entry);
            category.setTranslationId(translation.getTranslationId());
        }

        loggingService.log(LogLevel.INFO, String.format("createCategory %s UUID=%s %s", request.getName(), category.getCategoryId(), Message.CREATE.getMessage()));
        return CategoryResponseId.builder()
                .categoryId(category.getCategoryId())
                .build();
    }

    public void patchCategoryById(String categoryId, Category request, String acceptLanguage) throws JsonProcessingException {
        String finalAcceptLanguage = CheckHeader.checkHeaderLanguage(acceptLanguage);
        Map<String, String> fieldErrors = new ResponseErrorMap<>();

        Category category = categoryRepository.findByCategoryId(categoryId).orElseThrow(() -> ExceptionTools.notFoundException(".categoryNotFound", finalAcceptLanguage, categoryId));
        Translation translation = translationRepository.findAllByTranslationId(category.getTranslationId())
                .orElseThrow(() -> ExceptionTools.notFoundException(".translationNotFound", finalAcceptLanguage, categoryId));

        validateRequest(request, category, finalAcceptLanguage, fieldErrors, false);
        updateTranslation(category, request.getLanguage(), translation);

        category.setUpdatedAt(Instant.now().getEpochSecond());
        categoryRepository.save(category);
        loggingService.log(LogLevel.INFO, String.format("patchCategoryById %s %s", categoryId, Message.UPDATE.getMessage()));
    }

    public void deleteCategoryById(String categoryId, String acceptLanguage) {
        String finalAcceptLanguage = CheckHeader.checkHeaderLanguage(acceptLanguage);
        Category category = categoryRepository.findByCategoryId(categoryId).orElseThrow(() -> ExceptionTools.notFoundException(".categoryNotFound", finalAcceptLanguage, categoryId));
        categoryRepository.delete(category);
        loggingService.log(LogLevel.INFO, String.format("deleteCategoryById %s NAME=%s %s", categoryId, category.getName(), Message.DELETE.getMessage()));
    }

    private void validateRequest(Category request, Category category, String finalAcceptLanguage, Map<String, String> fieldErrors, boolean required) {
        fieldErrors.put("language", Validator.validateLanguage(request.getLanguage(), finalAcceptLanguage));

        updateField(request.getName(), required, category, fieldErrors, "name",
                (name) -> serviceHelper.updateNameField(category::setName, name, finalAcceptLanguage), finalAcceptLanguage);

        updateField(request.getState(), required, category, fieldErrors, "state",
                (state) -> serviceHelper.updateState(category::setState, state, finalAcceptLanguage), finalAcceptLanguage);

        updateField(request.getDescription(), required, category, fieldErrors, "description",
                (description) -> serviceHelper.updateVarcharField(category::setDescription, description, "description", finalAcceptLanguage), finalAcceptLanguage);

        if (fieldErrors.size() > 0)
            throw new BadFieldsResponse(HttpStatus.BAD_REQUEST, fieldErrors);
    }

    private <T> void updateField(T value, boolean required, Category category, Map<String, String> fieldErrors, String fieldName, FieldUpdateFunction<T> updateFunction, String finalAcceptLanguage) {
        if (value != null || required) {
            if (Objects.equals(fieldName, "name") && category.getMenu() != null) {
                List<Category> sameParent = categoryRepository.findAllByMenu_MenuId(category.getMenu().getMenuId());
                sameParent.remove(category);
                ExistEntity<CategoryTranslationEntry> existEntity = new ExistEntity<>(translationRepository);
                existEntity.checkExistEntity(Validator.removeExtraSpaces((String) value), sameParent, finalAcceptLanguage, fieldErrors);
            }
            String error = updateFunction.updateField(value);
            if (error != null) {
                fieldErrors.put(fieldName, error);
            }
        }
    }

    private void updateTranslation(Category category, String language, Translation translation) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        TypeReference<Content<CategoryTranslationEntry>> typeReference = new TypeReference<>() {
        };
        Content<CategoryTranslationEntry> content = objectMapper.readValue(translation.getContent(), typeReference);
        Map<String, CategoryTranslationEntry> languagesMap = content.getContent();
        if (languagesMap.get(language) != null) {
            if (category.getName() == null)
                category.setName(languagesMap.get(language).getName());
            if (category.getDescription() == null) {
                category.setDescription(content.getContent().get(language).getDescription());
            } else if (category.getDescription().equals("")) {
                category.setDescription(null);
            }
        }
        languagesMap.put(language, new CategoryTranslationEntry(category.getName(), category.getDescription()));
        translation.setContent(objectMapper.writeValueAsString(content));
        translation.setUpdatedAt(Instant.now().getEpochSecond());
    }
}