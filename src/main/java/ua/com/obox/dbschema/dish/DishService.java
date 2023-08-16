package ua.com.obox.dbschema.dish;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import ua.com.obox.dbschema.category.Category;
import ua.com.obox.dbschema.category.CategoryRepository;
import ua.com.obox.dbschema.tools.Validator;
import ua.com.obox.dbschema.tools.exception.Message;
import ua.com.obox.dbschema.tools.logging.LogLevel;
import ua.com.obox.dbschema.tools.logging.LoggingService;
import ua.com.obox.dbschema.tools.ftp.UploadDishImageFTP;
import ua.com.obox.dbschema.tools.services.AbstractResponseService;
import ua.com.obox.dbschema.tools.services.LoggingResponseHelper;
import ua.com.obox.dbschema.tools.services.UpdateServiceHelper;

@Service
@RequiredArgsConstructor
public class DishService extends AbstractResponseService {

    private final DishRepository dishRepository;
    private final CategoryRepository categoryRepository;
    private final LoggingService loggingService;
    private final UploadDishImageFTP dishImageFTP;
    private final DishServiceHelper dishServiceHelper;
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
        String associatedId;
        Dish dish = new Dish();
        String image = (request.getImage() != null) ? request.getImage() : "";
        Category category = null;
        loggingMessage = "createDish";
        responseMessage = String.format("Category with id %s", request.getCategory_id());

        request.setCategoryIdForDish(request.getCategory_id());
        if (request.getPrice() == null) {
            loggingService.log(LogLevel.ERROR, loggingMessage + " The price cannot be an empty");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The price cannot be an empty");
        }

        category = categoryRepository.findByCategoryId(request.getCategory_id()).orElseThrow(() -> {
            badRequestResponse(request.getCategory().getCategoryId());
            return null;
        });
        request.setCategoryIdForDish(request.getCategory_id());

        associatedId = dishServiceHelper.getAssociatedIdForDish(request.getCategory_id(), dishRepository, loggingService);

        dish = Dish.builder()
                .name(request.getName().trim()) // delete whitespaces
                .description(description)
                .associatedId(associatedId)
                .category(category)
                .build();

        serviceHelper.updateNameField(dish::setName, request.getName(), "Name", loggingMessage, loggingService);
        serviceHelper.updatePriceField(dish::setPrice, request.getPrice(), "Price", loggingMessage, loggingService, 100_000);
        serviceHelper.updateIntegerField(dish::setWeight, request.getWeight(), "Weight", loggingMessage, loggingService, 100_000);
        serviceHelper.updateIntegerField(dish::setCalories, request.getCalories(), "Weight", loggingMessage, loggingService, 30_000);
        serviceHelper.updateVarcharField(dish::setDescription, request.getDescription(), "Description", loggingMessage, loggingService);
        serviceHelper.updateVarcharField(dish::setAllergens, request.getAllergens(), "Allergens", loggingMessage, loggingService);
        serviceHelper.updateVarcharField(dish::setTags, request.getTags(), "Tags", loggingMessage, loggingService);
        serviceHelper.updateState(dish::setState, request.getState(), "State", loggingMessage, loggingService);

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
        String image = (request.getImage() != null) ? request.getImage() : "";
        loggingMessage = "patchDishById";
        responseMessage = String.format("Dish with id %s", dishId);
        var dishInfo = dishRepository.findByDishId(dishId);

        dish = dishInfo.orElseThrow(() -> {
            notFoundResponse(dishId);
            return null;
        });

        if (!image.isEmpty() && Validator.validateImage(image, loggingService)) {
            dishImageFTP.deleteImage(dish.getImageUrl(), loggingService); // delete old image
            dish.setImageUrl(dishImageFTP.uploadImage(request.getImage(), dish.getDishId(), loggingService)); // upload new image
        }

        dishServiceHelper.updateNameIfNeeded(request.getName(), dish, loggingMessage, loggingService, serviceHelper);
        dishServiceHelper.updatePriceIfNeeded(request.getPrice(), dish, loggingMessage, loggingService, serviceHelper);
        dishServiceHelper.updateStateIfNeeded(request.getState(), dish, loggingMessage, loggingService, serviceHelper);

        if (request.getCategory_id() != null) {
            categoryRepository.findByCategoryId(request.getCategory_id()).orElseThrow(() -> {
                badRequestResponse(request.getCategory_id());
                return null;
            });
            dish.setCategoryIdForDish(request.getCategory_id());
            dish.setCategory(dish.getCategory());
        }

        serviceHelper.updateVarcharField(dish::setDescription, request.getDescription(), "Description", loggingMessage, loggingService);
        serviceHelper.updateVarcharField(dish::setAllergens, request.getAllergens(), "Allergens", loggingMessage, loggingService);
        serviceHelper.updateVarcharField(dish::setTags, request.getTags(), "Tags", loggingMessage, loggingService);
        serviceHelper.updateIntegerField(dish::setCalories, request.getCalories(), "Calories", loggingMessage, loggingService, 30000);
        serviceHelper.updateIntegerField(dish::setWeight, request.getWeight(), "Weight", loggingMessage, loggingService, 100000);

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

    @Override
    public void badRequestResponse(String entityId) {
        LoggingResponseHelper.loggingThrowException(
                entityId,
                LogLevel.ERROR, HttpStatus.BAD_REQUEST,
                loggingMessage, responseMessage + Message.NOT_FOUND.getMessage(),
                loggingService);
    }
}
