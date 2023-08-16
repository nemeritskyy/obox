package ua.com.obox.dbschema.dish;

import lombok.RequiredArgsConstructor;
import org.hibernate.Session;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import ua.com.obox.dbschema.category.Category;
import ua.com.obox.dbschema.category.CategoryRepository;
import ua.com.obox.dbschema.tools.RequiredServiceHelper;
import ua.com.obox.dbschema.tools.Validator;
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
import java.util.Map;

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
        var dishInfo = dishRepository.findByDishId(dishId);

        dish = dishInfo.orElseThrow(() -> {
            notFoundResponse(dishId);
            return null;
        });

        loggingService.log(LogLevel.INFO, String.format("%s %s", loggingMessage, dishId));
        return DishResponse.builder()
                .dishId(dish.getDishId())
                .categoryId(dish.getCategory().getCategoryId())
                .name(dish.getName())
                .description(dish.getDescription())
                .price(dish.getPrice())
                .weight(dish.getWeight())
                .calories(dish.getCalories())
                .imageUrl(dish.getImageUrl())
                .state(dish.getState())
                .allergens(dish.getAllergens())
                .tags(dish.getTags())
                .associatedId(dish.getAssociatedId())
                .build();
    }

    public DishResponseId createDish(Dish request) {
        String associatedId = null;
        Map<String, String> fieldErrors = new ResponseErrorMap<>();
        String image = (request.getImage() != null) ? request.getImage() : "";
        Category category;

        loggingMessage = "createDish";
        responseMessage = String.format("Category with id %s", request.getCategory_id());

        category = categoryRepository.findByCategoryId(request.getCategory_id()).orElseGet(() -> {
            fieldErrors.put("category_id", responseMessage + Message.NOT_FOUND.getMessage());
            return null;
        });

        if (category != null) {
            request.setCategoryIdForDish(request.getCategory_id());
            associatedId = requiredServiceHelper.getAssociatedIdForDish(request.getCategory_id(), dishRepository, loggingService);
        }

        Dish dish = Dish.builder()
                .category(category)
                .build();

        fieldErrors.put("name", serviceHelper.updateNameField(dish::setName, request.getName(), "Name", loggingMessage, loggingService));
        fieldErrors.put("price", serviceHelper.updatePriceField(dish::setPrice, request.getPrice(), "Price", loggingMessage, loggingService, 100_000));
        fieldErrors.put("weight", serviceHelper.updateIntegerField(dish::setWeight, request.getWeight(), "Weight", loggingMessage, loggingService, 100_000));
        fieldErrors.put("calories", serviceHelper.updateIntegerField(dish::setCalories, request.getCalories(), "Calories", loggingMessage, loggingService, 30_000));
        fieldErrors.put("description", serviceHelper.updateVarcharField(dish::setDescription, request.getDescription(), "Description", loggingMessage, loggingService));
        fieldErrors.put("allergens", serviceHelper.updateVarcharField(dish::setAllergens, request.getAllergens(), "Allergens", loggingMessage, loggingService));
        fieldErrors.put("tags", serviceHelper.updateVarcharField(dish::setTags, request.getTags(), "Tags", loggingMessage, loggingService));
        fieldErrors.put("state", serviceHelper.updateState(dish::setState, request.getState(), "State", loggingMessage, loggingService));

        if (fieldErrors.size() > 0)
            throw new BadFieldsResponse(HttpStatus.BAD_REQUEST, fieldErrors);

        dish.setAssociatedId(associatedId);

        if (!image.isEmpty() && Validator.validateImage(image, loggingService)) {
            dish.setImageUrl(dishImageFTP.uploadImage(request.getImage(), dish.getDishId(), loggingService));
        }

        dishRepository.save(dish);

        loggingService.log(LogLevel.INFO, String.format("%s %s UUID=%s %s", loggingMessage, request.getName(), dish.getDishId(), Message.CREATE.getMessage()));
        return DishResponseId.builder()
                .dishId(dish.getDishId())
                .build();
    }

    public void patchDishById(String dishId, Dish request) {
        Dish dish;
        Map<String, String> fieldErrors = new ResponseErrorMap<>();
        String image = (request.getImage() != null) ? request.getImage() : "";
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

        if (request.getName() != null)
            fieldErrors.put("name", requiredServiceHelper.updateNameIfNeeded(request.getName(), dish, loggingMessage, loggingService, serviceHelper));
        if (request.getPrice() != null)
            fieldErrors.put("price", requiredServiceHelper.updatePriceIfNeeded(request.getPrice(), dish, loggingMessage, loggingService, serviceHelper));
        if (request.getState() != null)
            fieldErrors.put("state", serviceHelper.updateState(dish::setState, request.getState(), "State", loggingMessage, loggingService));

        if (request.getCategory_id() != null) {
            categoryRepository.findByCategoryId(request.getCategory_id()).orElseGet(() -> {
                fieldErrors.put("category_id", request.getCategory_id() + Message.NOT_FOUND.getMessage());
                return null;
            });
            dish.setCategoryIdForDish(request.getCategory_id());
            dish.setCategory(dish.getCategory());
        }

        if (request.getDescription() != null)
            fieldErrors.put("state", serviceHelper.updateVarcharField(dish::setDescription, request.getDescription(), "Description", loggingMessage, loggingService));

        if (request.getAllergens() != null)
            fieldErrors.put("allergens", serviceHelper.updateVarcharField(dish::setAllergens, request.getAllergens(), "Allergens", loggingMessage, loggingService));

        if (request.getTags() != null)
            fieldErrors.put("tags", serviceHelper.updateVarcharField(dish::setTags, request.getTags(), "Tags", loggingMessage, loggingService));

        if (request.getCalories() != null)
            fieldErrors.put("calories", serviceHelper.updateIntegerField(dish::setCalories, request.getCalories(), "Calories", loggingMessage, loggingService, 30000));

        if (request.getWeight() != null)
            fieldErrors.put("weight", serviceHelper.updateIntegerField(dish::setWeight, request.getWeight(), "Weight", loggingMessage, loggingService, 100000));

        if (fieldErrors.size() > 0)
            throw new BadFieldsResponse(HttpStatus.BAD_REQUEST, fieldErrors);

        if (!image.isEmpty() && Validator.validateImage(image, loggingService)) {
            dishImageFTP.deleteImage(dish.getImageUrl(), loggingService); // delete old image
            dish.setImageUrl(dishImageFTP.uploadImage(request.getImage(), dish.getDishId(), loggingService)); // upload new image
        }

        dishRepository.save(dish);
        loggingService.log(LogLevel.INFO, loggingMessage + Message.UPDATE.getMessage());
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
            dishImageFTP.deleteImage(dish.getImageUrl(), loggingService);
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

