package ua.com.obox.dbschema.dish;

import lombok.RequiredArgsConstructor;
import org.hibernate.Session;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import ua.com.obox.dbschema.attachment.AttachmentRepository;
import ua.com.obox.dbschema.category.Category;
import ua.com.obox.dbschema.category.CategoryRepository;
import ua.com.obox.dbschema.tools.RequiredServiceHelper;
import ua.com.obox.dbschema.tools.Validator;
import ua.com.obox.dbschema.tools.configuration.ValidationConfiguration;
import ua.com.obox.dbschema.tools.exception.ExceptionTools;
import ua.com.obox.dbschema.tools.exception.Message;
import ua.com.obox.dbschema.tools.logging.LogLevel;
import ua.com.obox.dbschema.tools.logging.LoggingService;
import ua.com.obox.dbschema.tools.response.BadFieldsResponse;
import ua.com.obox.dbschema.tools.response.ResponseErrorMap;
import ua.com.obox.dbschema.tools.services.UpdateServiceHelper;
import ua.com.obox.dbschema.tools.translation.CheckHeader;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DishService {
    @PersistenceContext
    private EntityManager entityManager;
    private final DishRepository dishRepository;
    private final CategoryRepository categoryRepository;
    private final AttachmentRepository attachmentRepository;
    private final LoggingService loggingService;
    private final UpdateServiceHelper serviceHelper;
    private final RequiredServiceHelper requiredServiceHelper;
    private final ResourceBundle translation = ResourceBundle.getBundle("translation.messages");

    public DishResponse getDishById(String dishId, String acceptLanguage) {
        String finalAcceptLanguage = CheckHeader.checkHeaderLanguage(acceptLanguage);

        try (Session session = entityManager.unwrap(Session.class)) {
            var dishInfo = dishRepository.findByDishId(dishId);

            Dish dish = dishInfo.orElseThrow(() -> {
                ExceptionTools.notFoundResponse(".dishNotFound", finalAcceptLanguage, dishId);
                return null;
            });
            session.evict(dish); // unbind the session

            List<String> allergens = new ArrayList<>();
            if (dish.getAllergens() != null)
                allergens.addAll(Arrays.stream(dish.getAllergens().split("::")).toList());
            Collections.sort(allergens);

            List<String> tags = new ArrayList<>();
            if (dish.getTags() != null)
                tags.addAll(Arrays.stream(dish.getTags().split("::")).toList());
            Collections.sort(tags);

            loggingService.log(LogLevel.INFO, String.format("getDishById %s", dishId));
            return DishResponse.builder()
                    .dishId(dish.getDishId())
                    .categoryId(dish.getCategory().getCategoryId())
                    .name(dish.getName())
                    .description(dish.getDescription())
                    .cookingTime(dish.getCooking_time())
                    .price(dish.getPrice())
                    .specialPrice(dish.getSpecialPrice())
                    .weight(dish.getWeight())
                    .weightUnit(dish.getWeight_unit())
                    .calories(dish.getCalories())
                    .inStock(dish.getIn_stock())
                    .state(dish.getState())
                    .allergens(allergens)
                    .tags(tags)
                    .associatedId(dish.getAssociatedId())
                    .image(dish.getImage())
                    .build();
        }
    }

    public DishResponseId createDish(Dish request, String acceptLanguage) {
        String finalAcceptLanguage = CheckHeader.checkHeaderLanguage(acceptLanguage);
        String associatedId = null;
        Map<String, String> fieldErrors = new ResponseErrorMap<>();

        Category category = categoryRepository.findByCategoryId(request.getCategory_id()).orElseGet(() -> {
            fieldErrors.put("category_id", String.format(translation.getString(finalAcceptLanguage + ".categoryNotFound"), request.getCategory_id()));
            return null;
        });

        if (category != null) {
            request.setCategoryIdForDish(request.getCategory_id());
            associatedId = requiredServiceHelper.getAssociatedIdForDish(request.getCategory_id(), dishRepository, finalAcceptLanguage);
        }

        Dish dish = Dish.builder()
                .category(category)
                .build();

        if (request.getName() != null && !dishRepository.findAllByCategory_CategoryIdAndName(request.getCategory_id(), Validator.removeExtraSpaces(request.getName())).isEmpty()) {
            fieldErrors.put("name", translation.getString(finalAcceptLanguage + ".dishExists"));
        } else {
            fieldErrors.put("name", serviceHelper.updateNameField(dish::setName, request.getName(), finalAcceptLanguage));
        }
        fieldErrors.put("price", serviceHelper.updatePriceField(dish::setPrice, request.getPrice(), ValidationConfiguration.MAX_PRICE, "price", false, finalAcceptLanguage));
        if (request.getSpecialPrice() != null)
            fieldErrors.put("special_price", serviceHelper.updatePriceField(dish::setSpecialPrice, request.getSpecialPrice(), ValidationConfiguration.MAX_PRICE, "specialPrice", true, finalAcceptLanguage));
        fieldErrors.put("cooking_time", serviceHelper.updateIntegerField(dish::setCooking_time, request.getCooking_time(), ValidationConfiguration.MAX_COOKING_TIME, "cookingTime", finalAcceptLanguage));
        fieldErrors.put("weight", serviceHelper.updateWeightField(dish::setWeight, request.getWeight(), finalAcceptLanguage));
        fieldErrors.put("calories", serviceHelper.updateIntegerField(dish::setCalories, request.getCalories(), ValidationConfiguration.MAX_CALORIES, "calories", finalAcceptLanguage));
        fieldErrors.put("description", serviceHelper.updateVarcharField(dish::setDescription, request.getDescription(), "description", finalAcceptLanguage));

        if (request.getAllergens() != null) {
            dish.setAllergens(request.getAllergens());
        }

        if (request.getTags() != null) {
            dish.setTags(request.getTags());
        }

        fieldErrors.put("weight_unit", serviceHelper.updateWeightUnit(dish::setWeight_unit, request.getWeight_unit(), finalAcceptLanguage));

        fieldErrors.put("in_stock", serviceHelper.updateState(dish::setIn_stock, request.getIn_stock(), finalAcceptLanguage));
        fieldErrors.put("state", serviceHelper.updateState(dish::setState, request.getState(), finalAcceptLanguage));

        if (fieldErrors.size() > 0)
            throw new BadFieldsResponse(HttpStatus.BAD_REQUEST, fieldErrors);

        dish.setAssociatedId(associatedId);
        dish.setCreatedAt(Instant.now().getEpochSecond());
        dish.setUpdatedAt(Instant.now().getEpochSecond());
        dishRepository.save(dish);

        loggingService.log(LogLevel.INFO, String.format("createDish %s UUID=%s %s", request.getName(), dish.getDishId(), Message.CREATE.getMessage()));
        return DishResponseId.builder()
                .dishId(dish.getDishId())
                .build();
    }

    public void patchDishById(String dishId, Dish request, String acceptLanguage) {
        String finalAcceptLanguage = CheckHeader.checkHeaderLanguage(acceptLanguage);
        Map<String, String> fieldErrors = new ResponseErrorMap<>();

        try (Session session = entityManager.unwrap(Session.class)) {
            var dishInfo = dishRepository.findByDishId(dishId);

            Dish dish = dishInfo.orElseThrow(() -> {
                ExceptionTools.notFoundResponse(".dishNotFound", finalAcceptLanguage, dishId);
                return null;
            });

            session.evict(dish); // unbind the session

            if (request.getName() != null && !dish.getName().equals(Validator.removeExtraSpaces(request.getName()))) {
                if (!dishRepository.findAllByCategory_CategoryIdAndName(dish.getCategory().getCategoryId(), Validator.removeExtraSpaces(request.getName())).isEmpty()) {
                    fieldErrors.put("name", translation.getString(finalAcceptLanguage + ".dishExists"));
                } else {
                    fieldErrors.put("name", requiredServiceHelper.updateNameIfNeeded(request.getName(), dish, finalAcceptLanguage));
                }
            }
            if (request.getPrice() != null)
                fieldErrors.put("price", requiredServiceHelper.updatePriceIfNeeded(request.getPrice(), dish, false, finalAcceptLanguage));
            if (request.getSpecialPrice() != null)
                fieldErrors.put("special_price", requiredServiceHelper.updatePriceIfNeeded(request.getSpecialPrice(), dish, true, finalAcceptLanguage));
            if (request.getWeight_unit() != null) {
                fieldErrors.put("weight_unit", serviceHelper.updateWeightUnit(dish::setWeight_unit, request.getWeight_unit(), finalAcceptLanguage));
            }
            if (request.getIn_stock() != null)
                fieldErrors.put("in_stock", serviceHelper.updateState(dish::setIn_stock, request.getIn_stock(), finalAcceptLanguage));
            if (request.getState() != null)
                fieldErrors.put("state", serviceHelper.updateState(dish::setState, request.getState(), finalAcceptLanguage));

            if (request.getCategory_id() != null) {
                categoryRepository.findByCategoryId(request.getCategory_id()).orElseGet(() -> {
                    fieldErrors.put("category_id", String.format(translation.getString(finalAcceptLanguage + ".categoryNotFound"), request.getCategory_id()));
                    return null;
                });
                dish.setCategoryIdForDish(request.getCategory_id());
                dish.setCategory(dish.getCategory());
            }

            if (request.getDescription() != null)
                fieldErrors.put("description", serviceHelper.updateVarcharField(dish::setDescription, request.getDescription(), "description", finalAcceptLanguage));

            if (request.getListAllergens() != null) {
                System.out.println(request.getListAllergens());
                String sortedAndJoinedAllergens = request.getListAllergens().stream()
                        .sorted()
                        .collect(Collectors.joining("::"));
                if (request.getListAllergens().size() == 0)
                    sortedAndJoinedAllergens = null;
                dish.setAllergens(sortedAndJoinedAllergens);
            }

            if (request.getListTags() != null) {
                System.out.println(request.getListTags());
                String sortedAndJoinedTags = request.getListTags().stream()
                        .sorted()
                        .collect(Collectors.joining("::"));
                if (request.getListTags().size() == 0)
                    sortedAndJoinedTags = null;
                dish.setTags(sortedAndJoinedTags);
            }

            if (request.getCooking_time() != null)
                fieldErrors.put("cooking_time", serviceHelper.updateIntegerField(dish::setCooking_time, request.getCooking_time(), ValidationConfiguration.MAX_COOKING_TIME, "cookingTime", finalAcceptLanguage));

            if (request.getCalories() != null)
                fieldErrors.put("calories", serviceHelper.updateIntegerField(dish::setCalories, request.getCalories(), ValidationConfiguration.MAX_CALORIES, "calories", finalAcceptLanguage));

            if (request.getWeight() != null)
                fieldErrors.put("weight", serviceHelper.updateWeightField(dish::setWeight, request.getWeight(), finalAcceptLanguage));

            if (fieldErrors.size() > 0)
                throw new BadFieldsResponse(HttpStatus.BAD_REQUEST, fieldErrors);

            dish.setUpdatedAt(Instant.now().getEpochSecond());
            dishRepository.save(dish);
            loggingService.log(LogLevel.INFO, String.format("patchDishById %s %s", dishId, Message.UPDATE.getMessage()));
        }
    }


    public void deleteDishById(String dishId, String acceptLanguage) {
        String finalAcceptLanguage = CheckHeader.checkHeaderLanguage(acceptLanguage);

        var dishInfo = dishRepository.findByDishId(dishId);

        Dish dish = dishInfo.orElseThrow(() -> {
            ExceptionTools.notFoundResponse(".dishNotFound", finalAcceptLanguage, dishId);
            return null;
        });

        dishRepository.delete(dish);
        loggingService.log(LogLevel.INFO, String.format("deleteDishById %s NAME=%s %s", dishId, dish.getName(), Message.DELETE.getMessage()));
    }

    public void setPrimaryImage(Dish request, String dishId, String acceptLanguage) {
        String finalAcceptLanguage = CheckHeader.checkHeaderLanguage(acceptLanguage);
        Map<String, String> fieldErrors = new ResponseErrorMap<>();

        var dishInfo = dishRepository.findByDishId(dishId);

        Dish dish = dishInfo.orElseThrow(() -> {
            ExceptionTools.notFoundResponse(".dishNotFound", finalAcceptLanguage, dishId);
            return null;
        });

        var attachmentInfo = attachmentRepository.findByAttachmentId(request.getImage());

        attachmentInfo.orElseGet(() -> {
            fieldErrors.put("image", String.format(translation.getString(finalAcceptLanguage + ".attachmentNotFound"), request.getImage()));
            return null;
        });

        if (fieldErrors.size() > 0)
            throw new BadFieldsResponse(HttpStatus.BAD_REQUEST, fieldErrors);

        dish.setImage(request.getImage());
        loggingService.log(LogLevel.INFO, String.format("setPrimaryImage set primary image %s for dish with id %s", request.getImage(), dishId));
    }
}

