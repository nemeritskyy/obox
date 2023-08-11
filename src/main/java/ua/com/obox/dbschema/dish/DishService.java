package ua.com.obox.dbschema.dish;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import ua.com.obox.dbschema.category.Category;
import ua.com.obox.dbschema.category.CategoryRepository;
import ua.com.obox.dbschema.tools.Validator;
import ua.com.obox.dbschema.tools.exception.ExceptionTools;
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
        String image = (request.getImage() != null) ? request.getImage() : "";
        Category category = null;

        loggingMessage = "createDish";
        responseMessage = String.format("Category with id %s", request.getCategory_id());

        request.setCategoryIdForDish(request.getCategory_id());

        if (request.getCategory_id() != null) {
            category = categoryRepository.findByCategoryId(request.getCategory().getCategoryId()).orElseThrow(() -> {
                badRequestResponse(request.getCategory().getCategoryId());
                return null;
            });
        }

        associatedId = dishServiceHelper.getAssociatedIdForDish(request.getCategory_id(), dishRepository, loggingService);

        Dish dish = Dish.builder()
                .associatedId(associatedId)
                .category(category)
                .state(request.getState())
                .build();

        serviceHelper.updateNameField(dish::setName, request.getName(), "Name", loggingMessage, loggingService);
        serviceHelper.updatePriceField(dish::setPrice, request.getPrice(), "Price", loggingMessage, loggingService, 100_000);
        serviceHelper.updateIntegerField(dish::setWeight, request.getWeight(), "Weight", loggingMessage, loggingService, 100_000);
        serviceHelper.updateIntegerField(dish::setCalories, request.getCalories(), "Weight", loggingMessage, loggingService, 30_000);
        serviceHelper.updateVarcharField(dish::setDescription, request.getDescription(), "Description", loggingMessage, loggingService);
        serviceHelper.updateVarcharField(dish::setAllergens, request.getAllergens(), "Allergens", loggingMessage, loggingService);
        serviceHelper.updateVarcharField(dish::setTags, request.getTags(), "Tags", loggingMessage, loggingService);

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
        String image = (request.getImage() != null) ? request.getImage() : "";
        loggingMessage = ExceptionTools.generateLoggingMessage("patchDishById", dishId);
        var dishInfo = dishRepository.findByDishId(dishId);
        Dish dish = dishInfo.orElseThrow(() -> {
            loggingService.log(LogLevel.ERROR, loggingMessage + Message.NOT_FOUND.getMessage());
            return new ResponseStatusException(HttpStatus.NOT_FOUND, "Dish with id " + dishId + Message.NOT_FOUND.getMessage());
        });
        if (image.equals("empty")) {
            loggingService.log(LogLevel.ERROR, loggingMessage + " Image" + Message.NOT_EMPTY.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Image" + Message.NOT_EMPTY.getMessage());
        }
        if (!image.isEmpty() && Validator.validateImage(image, loggingService)) {
            dishImageFTP.deleteImage(dish.getImageUrl(), loggingService); // delete old image
            dish.setImageUrl(dishImageFTP.uploadImage(request.getImage(), dish.getDishId(), loggingService)); // upload new image
        }
        if (request.getName() != null) {
            Validator.validateName(loggingMessage, request.getName(), loggingService);
            dish.setName(request.getName().trim()); // delete whitespaces
        }

        dishServiceHelper.updateDishFromRequestNullEnable(dish, request, loggingMessage, loggingService);

        if (request.getPrice() != null) {
            Validator.positiveInteger("Price", request.getPrice(), 100000, loggingService); // validate price
            dish.setPrice(request.getPrice());
        }

        if (request.getCategory_id() != null) {
            Validator.checkUUID(loggingMessage, request.getCategory_id(), loggingService); // validate UUID
            dish.setCategoryIdForDish(request.getCategory_id()); // set new category id;
            dish.setCategory(dish.getCategory());
        }
        if (request.getState() != null) {
            Validator.validateState(loggingMessage, request.getState(), loggingService); // validate state
            dish.setState(request.getState());
        }
        dishRepository.save(dish);
        loggingService.log(LogLevel.INFO, loggingMessage + Message.UPDATE.getMessage());
    }

    public void deleteDishById(String dishId) {
        loggingMessage = ExceptionTools.generateLoggingMessage("deleteDishById", dishId);
        var dishInfo = dishRepository.findByDishId(dishId);
        Dish dish = dishInfo.orElseThrow(() -> {
            loggingService.log(LogLevel.ERROR, loggingMessage + Message.NOT_FOUND.getMessage());
            return new ResponseStatusException(HttpStatus.NOT_FOUND, "Dish with id " + dishId + Message.NOT_FOUND.getMessage());
        });
        if (dish.getImageUrl() != null) {
            dishImageFTP.deleteImage(dish.getImageUrl(), loggingService);
        }
        dishRepository.delete(dish);
        loggingService.log(LogLevel.INFO, loggingMessage + " name=" + dish.getName() + Message.DELETE.getMessage());
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
