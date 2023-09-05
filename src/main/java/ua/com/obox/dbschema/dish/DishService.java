package ua.com.obox.dbschema.dish;

import lombok.RequiredArgsConstructor;
import org.hibernate.Session;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import ua.com.obox.dbschema.category.Category;
import ua.com.obox.dbschema.category.CategoryRepository;
import ua.com.obox.dbschema.tools.RequiredServiceHelper;
import ua.com.obox.dbschema.tools.exception.Message;
import ua.com.obox.dbschema.tools.logging.LogLevel;
import ua.com.obox.dbschema.tools.logging.LoggingService;
import ua.com.obox.dbschema.tools.ftp.UploadDishImageFTP;
import ua.com.obox.dbschema.tools.response.BadFieldsResponse;
import ua.com.obox.dbschema.tools.response.ResponseErrorMap;
import ua.com.obox.dbschema.tools.services.AbstractResponseService;
import ua.com.obox.dbschema.tools.services.LoggingResponseHelper;
import ua.com.obox.dbschema.tools.services.UpdateServiceHelper;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DishService extends AbstractResponseService {
    @PersistenceContext
    private EntityManager entityManager;
    private final DishRepository dishRepository;
    private final CategoryRepository categoryRepository;
    private final LoggingService loggingService;
    private final UploadDishImageFTP dishImageFTP;
    private final RequiredServiceHelper requiredServiceHelper;
    private final UpdateServiceHelper serviceHelper;
    private String loggingMessage;
    private String responseMessage;

    public DishResponse getDishById(String dishId) {
        Dish dish;
        loggingMessage = "getDishById";
        responseMessage = String.format("Dish with id %s", dishId);
        try (Session session = entityManager.unwrap(Session.class)) {
            var dishInfo = dishRepository.findByDishId(dishId);

            dish = dishInfo.orElseThrow(() -> {
                notFoundResponse(dishId);
                return null;
            });
            session.evict(dish); // unbind the session
        }

        List<String> allergens = new ArrayList<>();
        if (dish.getAllergens() != null)
            allergens.addAll(Arrays.stream(dish.getAllergens().split("::")).toList());
        Collections.sort(allergens);

        List<String> tags = new ArrayList<>();
        if (dish.getTags() != null)
            tags.addAll(Arrays.stream(dish.getTags().split("::")).toList());
        Collections.sort(tags);

        loggingService.log(LogLevel.INFO, String.format("%s %s", loggingMessage, dishId));
        return DishResponse.builder()
                .dishId(dish.getDishId())
                .categoryId(dish.getCategory().getCategoryId())
                .name(dish.getName())
                .description(dish.getDescription())
                .price(dish.getPrice())
                .weight(dish.getWeight())
                .calories(dish.getCalories())
                .imageUrl(dish.getImageUrl() == null ? null : String.format("%s/%s/%s", "https://img.obox.com.ua", dish.getAssociatedId(), dish.getImageUrl()))
                .state(dish.getState())
                .allergens(allergens)
                .tags(tags)
                .associatedId(dish.getAssociatedId())
                .build();
    }

    public DishResponseId createDish(Dish request) {
        String associatedId = null;
        Map<String, String> fieldErrors = new ResponseErrorMap<>();
        Category category;

        loggingMessage = "createDish";
        responseMessage = String.format("Category with id %s", request.getCategory_id());

        category = categoryRepository.findByCategoryId(request.getCategory_id()).orElseGet(() -> {
            fieldErrors.put("category_id", responseMessage + Message.NOT_FOUND.getMessage());
            return null;
        });

        if (category != null) {
            request.setCategoryIdForDish(request.getCategory_id());
            associatedId = requiredServiceHelper.getAssociatedIdForDish(request.getCategory_id(), dishRepository);
        }

        Dish dish = Dish.builder()
                .category(category)
                .build();

        if (request.getName() != null && !dishRepository.findAllByCategory_CategoryIdAndName(request.getCategory_id(), request.getName().trim().replaceAll("\\s+", " ")).isEmpty()) {
            loggingMessage = Message.DISH_EXISTS.getMessage();
            fieldErrors.put("name", Message.DISH_EXISTS.getMessage());
        } else {
            fieldErrors.put("name", serviceHelper.updateNameField(dish::setName, request.getName(), "Name", loggingMessage));
        }
        fieldErrors.put("price", serviceHelper.updatePriceField(dish::setPrice, request.getPrice(), "Price", loggingMessage, 100_000));
        fieldErrors.put("weight", serviceHelper.updateIntegerField(dish::setWeight, request.getWeight(), "Weight", loggingMessage, 100_000));
        fieldErrors.put("calories", serviceHelper.updateIntegerField(dish::setCalories, request.getCalories(), "Calories", loggingMessage, 30_000));
        fieldErrors.put("description", serviceHelper.updateVarcharField(dish::setDescription, request.getDescription(), "Description", loggingMessage));

        if (request.getAllergens() != null) {
            dish.setAllergens(request.getAllergens());
        }

        if (request.getTags() != null) {
            dish.setTags(request.getTags());
        }

        fieldErrors.put("state", serviceHelper.updateState(dish::setState, request.getState(), "State", loggingMessage));

        if (fieldErrors.size() > 0)
            throw new BadFieldsResponse(HttpStatus.BAD_REQUEST, fieldErrors);

        dish.setAssociatedId(associatedId);

        dishRepository.save(dish);

        if (request.getImages() != null) {
            dish.setImageUrl(dishImageFTP.uploadImage(request.getImages(), associatedId, dish.getDishId(), loggingService));
        }

        loggingService.log(LogLevel.INFO, String.format("%s %s UUID=%s %s", loggingMessage, request.getName(), dish.getDishId(), Message.CREATE.getMessage()));
        return DishResponseId.builder()
                .dishId(dish.getDishId())
                .build();
    }

    public void patchDishById(String dishId, Dish request) {
        Dish dish;
        Map<String, String> fieldErrors = new ResponseErrorMap<>();
        loggingMessage = "patchDishById";
        responseMessage = String.format("Dish with id %s", dishId);

        try (Session session = entityManager.unwrap(Session.class)) {
            var dishInfo = dishRepository.findByDishId(dishId);

            dish = dishInfo.orElseThrow(() -> {
                notFoundResponse(dishId);
                return null;
            });

            session.evict(dish); // unbind the session
        }

        if (request.getName() != null && !dish.getName().equals(request.getName().trim().replaceAll("\\s+", " "))) {
            if (!dishRepository.findAllByCategory_CategoryIdAndName(dish.getCategory().getCategoryId(), request.getName().trim().replaceAll("\\s+", " ")).isEmpty()) {
                loggingMessage = Message.DISH_EXISTS.getMessage();
                fieldErrors.put("name", Message.DISH_EXISTS.getMessage());
            } else {
                fieldErrors.put("name", requiredServiceHelper.updateNameIfNeeded(request.getName(), dish, loggingMessage));
            }
        }

        if (request.getPrice() != null)
            fieldErrors.put("price", requiredServiceHelper.updatePriceIfNeeded(request.getPrice(), dish, loggingMessage));
        if (request.getState() != null)
            fieldErrors.put("state", serviceHelper.updateState(dish::setState, request.getState(), "State", loggingMessage));

        if (request.getCategory_id() != null) {
            categoryRepository.findByCategoryId(request.getCategory_id()).orElseGet(() -> {
                fieldErrors.put("category_id", request.getCategory_id() + Message.NOT_FOUND.getMessage());
                return null;
            });
            dish.setCategoryIdForDish(request.getCategory_id());
            dish.setCategory(dish.getCategory());
        }

        if (request.getDescription() != null)
            fieldErrors.put("state", serviceHelper.updateVarcharField(dish::setDescription, request.getDescription(), "Description", loggingMessage));

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

        if (request.getCalories() != null)
            fieldErrors.put("calories", serviceHelper.updateIntegerField(dish::setCalories, request.getCalories(), "Calories", loggingMessage, 30000));

        if (request.getWeight() != null)
            fieldErrors.put("weight", serviceHelper.updateIntegerField(dish::setWeight, request.getWeight(), "Weight", loggingMessage, 100000));

        if (fieldErrors.size() > 0)
            throw new BadFieldsResponse(HttpStatus.BAD_REQUEST, fieldErrors);

        if (request.getImages() != null) {
            UploadDishImageFTP.deleteImage(dish.getAssociatedId(), dish.getImageUrl()); // delete old image
            dish.setImageUrl(dishImageFTP.uploadImage(request.getImages(), dish.getAssociatedId(), dish.getDishId(), loggingService)); // upload new image
        }

        dishRepository.save(dish);
        loggingService.log(LogLevel.INFO, String.format("%s %s %s", loggingMessage, dishId, Message.UPDATE.getMessage()));
    }

    public void deleteDishById(String dishId) {
        Dish dish;
        loggingMessage = "deleteDishById";
        responseMessage = String.format("Dish with id %s", dishId);
        var dishInfo = dishRepository.findByDishId(dishId);

        dish = dishInfo.orElseThrow(() -> {
            notFoundResponse(dishId);
            return null;
        });

        if (dish.getImageUrl() != null) {
            UploadDishImageFTP.deleteImage(dish.getAssociatedId(), dish.getImageUrl());
        }

        dishRepository.delete(dish);
        loggingService.log(LogLevel.INFO, String.format("%s %s NAME=%s %s", loggingMessage, dishId, dish.getName(), Message.DELETE.getMessage()));
    }

    @Override
    public void notFoundResponse(String entityId) {
        LoggingResponseHelper.loggingThrowException(
                entityId,
                LogLevel.ERROR, HttpStatus.NOT_FOUND,
                loggingMessage, responseMessage + Message.NOT_FOUND.getMessage(),
                loggingService);
    }
}

