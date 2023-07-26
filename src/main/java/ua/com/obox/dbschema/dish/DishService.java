package ua.com.obox.dbschema.dish;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import ua.com.obox.dbschema.category.Category;
import ua.com.obox.dbschema.category.CategoryRepository;
import ua.com.obox.dbschema.tools.State;
import ua.com.obox.dbschema.tools.Validator;
import ua.com.obox.dbschema.tools.exception.ExceptionTools;
import ua.com.obox.dbschema.tools.exception.Message;
import ua.com.obox.dbschema.tools.logging.LogLevel;
import ua.com.obox.dbschema.tools.logging.LoggingService;
import ua.com.obox.dbschema.tools.ftp.UploadDishImageFTP;

@Service
@RequiredArgsConstructor
public class DishService {

    private final DishRepository dishRepository;
    private final CategoryRepository categoryRepository;
    private final LoggingService loggingService;
    private final UploadDishImageFTP dishImageFTP;
    private String loggingMessage;

    public DishResponse getDishById(String dishId) {
        loggingMessage = ExceptionTools.generateLoggingMessage("getDishById", dishId);
        var dishInfo = dishRepository.findByDishId(dishId);
        Dish dish = dishInfo.orElseThrow(() -> {
            loggingService.log(LogLevel.ERROR, loggingMessage + Message.NOT_FOUND.getMessage());
            return new ResponseStatusException(HttpStatus.NOT_FOUND, "Dish with id " + dishId + Message.NOT_FOUND.getMessage());
        });

        if (dish.getState().equals(State.DISABLE)) {
            loggingService.log(LogLevel.ERROR, loggingMessage + Message.HIDDEN.getMessage());
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Dish with id " + dishId + Message.HIDDEN.getMessage());
        }
        loggingService.log(LogLevel.INFO, loggingMessage);
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
                .build();
    }

    public DishResponseId createDish(Dish request) {
        String image = "";
        if (request.getImage() != null) {
            image = request.getImage();
        }
        loggingMessage = ExceptionTools.generateLoggingMessage("createDish", request.getCategory_id());
        request.setCategoryIdForDish(request.getCategory_id());
        Category category = categoryRepository.findByCategoryId(request.getCategory().getCategoryId()).orElseThrow(() -> {
            loggingService.log(LogLevel.ERROR, loggingMessage + Message.CATEGORY_NOT_FOUND.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Category with id " + request.getCategory().getCategoryId() + Message.NOT_FOUND.getMessage(), null);
        });
        Dish dish = Dish.builder()
                .name(request.getName().trim()) // delete whitespaces
                .description(request.getDescription().trim())
                .price(request.getPrice())
                .category(category)
                .calories(request.getCalories())
                .weight(request.getWeight())
                .state(request.getState())
                .build();
        dishRepository.save(dish);
        if (!image.isEmpty() && Validator.validateImage(image, loggingService)) {
            dish.setImageUrl(dishImageFTP.uploadImage(request.getImage(), dish.getDishId(), loggingService));
            dishRepository.save(dish);
        }
        loggingService.log(LogLevel.INFO, loggingMessage + " id=" + dish.getDishId() + Message.CREATE.getMessage());
        return DishResponseId.builder()
                .dishId(dish.getDishId())
                .build();
    }

    public void patchDishById(String dishId, Dish request) {
        String image = "";
        if (request.getImage() != null) {
            image = request.getImage();
        }
        loggingMessage = ExceptionTools.generateLoggingMessage("patchDishById", dishId);
        var dishInfo = dishRepository.findByDishId(dishId);
        Dish dish = dishInfo.orElseThrow(() -> {
            loggingService.log(LogLevel.ERROR, loggingMessage + Message.NOT_FOUND.getMessage());
            return new ResponseStatusException(HttpStatus.NOT_FOUND, "Dish with id " + dishId + Message.NOT_FOUND.getMessage());
        });

        if (!image.isEmpty() && Validator.validateImage(image, loggingService)) {
            dishImageFTP.deleteImage(dish.getImageUrl(), loggingService); // delete old image
            dish.setImageUrl(dishImageFTP.uploadImage(request.getImage(), dish.getDishId(), loggingService)); // upload new image
        }
        if (request.getName() != null) {
            Validator.validateName(loggingMessage, request.getName(), loggingService);
            dish.setName(request.getName().trim()); // delete whitespaces
        }
        if (request.getDescription() != null) {
            Validator.validateVarchar(loggingMessage, "Description", request.getDescription(), loggingService);
            dish.setDescription(request.getDescription().trim());
        }
        if (request.getPrice() != null) {
            Validator.positiveInteger("Price", request.getPrice(), 100000, loggingService); // validate price
            dish.setPrice(request.getPrice());
        }
        if (request.getCalories() != null) {
            Validator.positiveInteger("Calories", request.getCalories(), 30000, loggingService); // validate calories
            dish.setCalories(request.getCalories());
        }
        if (request.getWeight() != null) {
            Validator.positiveInteger("Weight", request.getWeight(), 100000, loggingService); // validate weight
            dish.setWeight(request.getWeight());
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

}
