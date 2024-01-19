package ua.com.obox.dbschema.dish;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import ua.com.obox.authserver.user.UserService;
import ua.com.obox.dbschema.attachment.Attachment;
import ua.com.obox.dbschema.attachment.AttachmentRepository;
import ua.com.obox.dbschema.category.Category;
import ua.com.obox.dbschema.category.CategoryRepository;
import ua.com.obox.dbschema.tools.FieldUpdateFunction;
import ua.com.obox.dbschema.tools.Validator;
import ua.com.obox.dbschema.tools.attachment.AttachmentTools;
import ua.com.obox.dbschema.tools.attachment.ReferenceType;
import ua.com.obox.dbschema.tools.configuration.ValidationConfiguration;
import ua.com.obox.dbschema.tools.exception.ExceptionTools;
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
import ua.com.obox.dbschema.translation.responsebody.DishTranslationEntry;

import java.time.Instant;
import java.util.*;

@Service
@RequiredArgsConstructor
public class DishService {
    private final DishRepository dishRepository;
    private final CategoryRepository categoryRepository;
    private final AttachmentRepository attachmentRepository;
    private final TranslationRepository translationRepository;
    private final UpdateServiceHelper serviceHelper;
    private final UserService userService;
    private final ResourceBundle translationContent = ResourceBundle.getBundle("translation.messages");
    @Value("${application.image-dns}")
    private String attachmentsDns;

    public DishResponse getDishById(String dishId, String acceptLanguage) throws JsonProcessingException {
        String finalAcceptLanguage = CheckHeader.checkHeaderLanguage(acceptLanguage);

        Dish dish = dishRepository.findByDishId(dishId).orElseThrow(() -> ExceptionTools.notFoundException(".dishNotFound", finalAcceptLanguage, dishId));
        userService.checkPermissionForUser(ReferenceType.dish, dishId, finalAcceptLanguage);

        Translation translation = translationRepository.findAllByTranslationId(dish.getTranslationId())
                .orElseThrow(() -> ExceptionTools.notFoundException(".translationNotFound", finalAcceptLanguage, dishId));

        ObjectMapper objectMapper = new ObjectMapper();
        Content<CategoryTranslationEntry> content = objectMapper.readValue(translation.getContent(), new TypeReference<>() {
        });

        return DishResponse.builder()
                .dishId(dish.getDishId())
                .categoryId(dish.getCategory().getCategoryId())
                .originalLanguage(dish.getOriginalLanguage())
                .translationId(dish.getTranslationId())
                .content(content)
                .cookingTime(dish.getCookingTime())
                .price(dish.getPrice())
                .specialPrice(dish.getSpecialPrice())
                .weight(dish.getWeight())
                .weightUnit(dish.getWeightUnit())
                .calories(dish.getCalories())
                .inStock(dish.getInStock())
                .state(dish.getState())
                .allergens(dish.getAllergens() == null ? null : Arrays.stream(dish.getAllergens().split(",")).toList())
                .marks(dish.getMarks() == null ? null : Arrays.stream(dish.getMarks().split(",")).toList())
                .image(AttachmentTools.getURL(dish, attachmentRepository, attachmentsDns))
                .build();

    }

    public DishResponseId createDish(Dish request, String acceptLanguage) throws JsonProcessingException {
        String finalAcceptLanguage = CheckHeader.checkHeaderLanguage(acceptLanguage);
        userService.checkPermissionForUser(ReferenceType.category, request.getCategoryId(), finalAcceptLanguage);
        Map<String, String> fieldErrors = new ResponseErrorMap<>();
        Optional<Category> category = categoryRepository.findByCategoryId(request.getCategoryId());
        if (category.isEmpty())
            fieldErrors.put("category_id", String.format(translationContent.getString(finalAcceptLanguage + ".categoryNotFound"), request.getCategoryId()));

        Dish dish = Dish.builder()
                .category(category.orElse(null))
                .build();

        validateRequest(request, dish, finalAcceptLanguage, fieldErrors, true);

        dish.setOriginalLanguage(request.getLanguage());
        dish.setCreatedAt(Instant.now().getEpochSecond());
        dish.setUpdatedAt(Instant.now().getEpochSecond());
        dishRepository.save(dish);

        {
            CreateTranslation<DishTranslationEntry> createTranslation = new CreateTranslation<>(translationRepository);
            DishTranslationEntry entry = new DishTranslationEntry(dish.getName(), dish.getDescription());
            Translation translation = createTranslation
                    .create(dish.getDishId(), "dish", request.getLanguage(), entry);
            dish.setTranslationId(translation.getTranslationId());
            dishRepository.save(dish);
        }

        return DishResponseId.builder()
                .dishId(dish.getDishId())
                .build();
    }

    public void patchDishById(String dishId, Dish request, String acceptLanguage) throws JsonProcessingException {
        String finalAcceptLanguage = CheckHeader.checkHeaderLanguage(acceptLanguage);
        Map<String, String> fieldErrors = new ResponseErrorMap<>();

        Dish dish = dishRepository.findByDishId(dishId).orElseThrow(() -> ExceptionTools.notFoundException(".dishNotFound", finalAcceptLanguage, dishId));
        userService.checkPermissionForUser(ReferenceType.dish, dishId, finalAcceptLanguage);

        Translation translation = translationRepository.findAllByTranslationId(dish.getTranslationId())
                .orElseThrow(() -> ExceptionTools.notFoundException(".translationNotFound", finalAcceptLanguage, dishId));

        if (request.getCategoryId() != null && !request.getCategoryId().isEmpty()) {
            Optional<Category> category = categoryRepository.findByCategoryId(request.getCategoryId());
            if (category.isEmpty())
                fieldErrors.put("category_id", String.format(translationContent.getString(finalAcceptLanguage + ".categoryNotFound"), request.getCategoryId()));
            dish.setCategory(category.orElse(null));
        }

        validateRequest(request, dish, finalAcceptLanguage, fieldErrors, false);
        updateTranslation(dish, request.getLanguage(), translation);

        dish.setUpdatedAt(Instant.now().getEpochSecond());
        dishRepository.save(dish);
    }


    public void deleteDishById(String dishId, String acceptLanguage) {
        String finalAcceptLanguage = CheckHeader.checkHeaderLanguage(acceptLanguage);
        Dish dish = dishRepository.findByDishId(dishId).orElseThrow(() -> ExceptionTools.notFoundException(".dishNotFound", finalAcceptLanguage, dishId));
        userService.checkPermissionForUser(ReferenceType.dish, dishId, finalAcceptLanguage);
        dishRepository.delete(dish);
    }

    public void setPrimaryImage(Dish request, String dishId, String acceptLanguage) {
        String finalAcceptLanguage = CheckHeader.checkHeaderLanguage(acceptLanguage);
        Map<String, String> fieldErrors = new ResponseErrorMap<>();

        Dish dish = dishRepository.findByDishId(dishId).orElseThrow(() -> ExceptionTools.notFoundException(".dishNotFound", finalAcceptLanguage, dishId));
        userService.checkPermissionForUser(ReferenceType.dish, dishId, finalAcceptLanguage);

        Optional<Attachment> attachment = attachmentRepository.findByAttachmentId(request.getImage());
        if (attachment.isEmpty())
            fieldErrors.put("image", String.format(translationContent.getString(finalAcceptLanguage + ".attachmentNotFound"), request.getImage()));

        if (fieldErrors.size() > 0)
            throw new BadFieldsResponse(HttpStatus.BAD_REQUEST, fieldErrors);

        dish.setImage(request.getImage());
        dishRepository.save(dish);
    }

    private void validateRequest(Dish request, Dish dish, String finalAcceptLanguage, Map<String, String> fieldErrors, boolean required) {
        fieldErrors.put("language", Validator.validateLanguage(request.getLanguage(), finalAcceptLanguage));

        updateField(request.getAllergensArray(), required, dish, fieldErrors, "allergens",
                (allergens) -> serviceHelper.updateAllergens(dish::setAllergens, allergens, finalAcceptLanguage), finalAcceptLanguage);

        updateField(request.getMarksArray(), required, dish, fieldErrors, "marks",
                (marks) -> serviceHelper.updateAllergens(dish::setMarks, marks, finalAcceptLanguage), finalAcceptLanguage);

        updateField(request.getName(), required, dish, fieldErrors, "name",
                (name) -> serviceHelper.updateNameField(dish::setName, name, finalAcceptLanguage), finalAcceptLanguage);

        updateField(request.getDescription(), required, dish, fieldErrors, "description",
                (description) -> serviceHelper.updateVarcharField(dish::setDescription, description, "description", finalAcceptLanguage), finalAcceptLanguage);

        updateField(request.getPrice(), required, dish, fieldErrors, "price",
                (price) -> serviceHelper.updatePriceField(dish::setPrice, price, ValidationConfiguration.MAX_PRICE, "price", false, finalAcceptLanguage), finalAcceptLanguage);

        updateField(request.getSpecialPrice(), false, dish, fieldErrors, "special_price",
                (specialPrice) -> serviceHelper.updatePriceField(dish::setSpecialPrice, specialPrice, ValidationConfiguration.MAX_PRICE, "specialPrice", true, finalAcceptLanguage), finalAcceptLanguage);

        updateField(request.getCookingTime(), required, dish, fieldErrors, "cooking_time",
                (cookingTime) -> serviceHelper.updateIntegerField(dish::setCookingTime, cookingTime, ValidationConfiguration.MAX_COOKING_TIME, "cookingTime", finalAcceptLanguage), finalAcceptLanguage);

        updateField(request.getCalories(), required, dish, fieldErrors, "calories",
                (calories) -> serviceHelper.updateIntegerField(dish::setCalories, calories, ValidationConfiguration.MAX_CALORIES, "calories", finalAcceptLanguage), finalAcceptLanguage);

        updateField(request.getWeight(), required, dish, fieldErrors, "weight",
                (weight) -> serviceHelper.updateWeightField(dish, weight, finalAcceptLanguage), finalAcceptLanguage);

        updateField(request.getWeightUnit(), required, dish, fieldErrors, "weight_unit",
                (weightUnit) -> serviceHelper.updateWeightUnit(dish, weightUnit, finalAcceptLanguage), finalAcceptLanguage);

        updateField("", required, dish, fieldErrors, null,
                (weight) -> serviceHelper.checkWeight(dish, request, fieldErrors, finalAcceptLanguage), finalAcceptLanguage);

        updateField(request.getInStock(), required, dish, fieldErrors, "in_stock",
                (inStock) -> serviceHelper.updateState(dish::setInStock, inStock, finalAcceptLanguage), finalAcceptLanguage);

        updateField(request.getState(), required, dish, fieldErrors, "state",
                (state) -> serviceHelper.updateState(dish::setState, state, finalAcceptLanguage), finalAcceptLanguage);

        if (fieldErrors.size() > 0)
            throw new BadFieldsResponse(HttpStatus.BAD_REQUEST, fieldErrors);
    }

    private <T> void updateField(T value, boolean required, Dish dish, Map<String, String> fieldErrors, String fieldName, FieldUpdateFunction<T> updateFunction, String finalAcceptLanguage) {
        if (value != null || required) {
            if (Objects.equals(fieldName, "name") && dish.getCategory() != null) {
                ExistEntity<DishTranslationEntry> existEntity = new ExistEntity<>(translationRepository);
                /**
                 * validate exists on menu level, because dish will to move from one category to other
                 * */
                List<Category> sameParentCategories = categoryRepository.findAllByMenu_MenuId(dish.getCategory().getMenu().getMenuId());
                List<Dish> allDishesForMenu = new ArrayList<>();
                for (Category category : sameParentCategories) {
                    allDishesForMenu.addAll(dishRepository.findAllByCategory_CategoryIdOrderByCreatedAtDesc(category.getCategoryId()));
                }
                allDishesForMenu.remove(dish);
                existEntity.checkExistEntity(Validator.removeExtraSpaces((String) value), allDishesForMenu, finalAcceptLanguage, fieldErrors);
            }
            String error = updateFunction.updateField(value);
            if (error != null) {
                fieldErrors.put(fieldName, error);
            }
        }
    }

    private void updateTranslation(Dish dish, String language, Translation translation) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        TypeReference<Content<DishTranslationEntry>> typeReference = new TypeReference<>() {
        };
        Content<DishTranslationEntry> content = objectMapper.readValue(translation.getContent(), typeReference);
        Map<String, DishTranslationEntry> languagesMap = content.getContent();
        if (languagesMap.get(language) != null) {
            if (dish.getName() == null)
                dish.setName(languagesMap.get(language).getName());
            if (dish.getDescription() == null) {
                dish.setDescription(content.getContent().get(language).getDescription());
            } else if (dish.getDescription().equals("")) {
                dish.setDescription(null);
            }
        }
        languagesMap.put(language, new DishTranslationEntry(dish.getName(), dish.getDescription()));
        translation.setContent(objectMapper.writeValueAsString(content));
        translation.setUpdatedAt(Instant.now().getEpochSecond());
    }
}