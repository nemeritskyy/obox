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

@Service
@RequiredArgsConstructor
public class DishService {

    private final DishRepository dishRepository;
    private final CategoryRepository categoryRepository;
    private final LoggingService loggingService;
    private final UploadDishImageFTP dishImageFTP;
    private final DishServiceHelper serviceHelper;
    private String loggingMessage;

    public DishResponse getDishById(String dishId) {
        loggingMessage = ExceptionTools.generateLoggingMessage("getDishById", dishId);
        String categoryUUID = null;
        var dishInfo = dishRepository.findByDishId(dishId);
        Dish dish = dishInfo.orElseThrow(() -> {
            loggingService.log(LogLevel.ERROR, loggingMessage + Message.NOT_FOUND.getMessage());
            return new ResponseStatusException(HttpStatus.NOT_FOUND, "Dish with id " + dishId + Message.NOT_FOUND.getMessage());
        });
        if (dish.getCategory() != null) {
            categoryUUID = dish.getCategory().getCategoryId();
        }

        loggingService.log(LogLevel.INFO, loggingMessage);
        return DishResponse.builder()
                .dishId(dish.getDishId())
                .categoryId(categoryUUID)
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
        String image = (request.getImage() != null) ? request.getImage() : "";
        String description = (request.getDescription() != null) ? request.getDescription().trim() : null;
        String allergens = (request.getAllergens() != null) ? request.getAllergens().trim() : null;
        String tags = (request.getTags() != null) ? request.getTags().trim() : null;
        Category category = null;

        loggingMessage = ExceptionTools.generateLoggingMessage("createDish", request.getCategory_id());
        request.setCategoryIdForDish(request.getCategory_id());
        if (request.getPrice() == null) {
            loggingService.log(LogLevel.ERROR, loggingMessage + " The price cannot be an empty");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The price cannot be an empty");
        }

        if (request.getCategory_id() != null) {
            category = categoryRepository.findByCategoryId(request.getCategory().getCategoryId()).orElseThrow(() -> {
                loggingService.log(LogLevel.ERROR, loggingMessage + Message.CATEGORY_NOT_FOUND.getMessage());
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Category with id " + request.getCategory().getCategoryId() + Message.NOT_FOUND.getMessage(), null);
            });
        }

        String associatedId = serviceHelper.getAssociatedIdForDish(request.getCategory_id(), dishRepository, loggingService);

        Dish dish = Dish.builder()
                .name(request.getName().trim()) // delete whitespaces
                .description(description)
                .associatedId(associatedId)
                .price(request.getPrice())
                .category(category)
                .calories(request.getCalories())
                .weight(request.getWeight())
                .allergens(allergens)
                .tags(tags)
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
        if (request.getDescription() != null) {
            if (!request.getDescription().trim().isEmpty()) {
                Validator.validateVarchar(loggingMessage, "Description", request.getDescription(), loggingService);
                dish.setDescription(request.getDescription().trim());
            } else {
                dish.setDescription(null);
            }
        }
        if (request.getAllergens() != null) {
            Validator.validateVarchar(loggingMessage, "Allergens", request.getAllergens(), loggingService);
            dish.setAllergens(request.getAllergens().trim());
        }
        if (request.getTags() != null) {
            Validator.validateVarchar(loggingMessage, "Tags", request.getTags(), loggingService);
            dish.setTags(request.getTags().trim());
        }
        if (request.getPrice() != null) {
            Validator.positiveInteger("Price", request.getPrice(), 100000, loggingService); // validate price
            dish.setPrice(request.getPrice());
        }
        if (request.getCalories() != null) {
            if (request.getCalories() == 0) {
                dish.setCalories(null);
            } else {
                Validator.positiveInteger("Calories", request.getCalories(), 30000, loggingService); // validate calories
                dish.setCalories(request.getCalories());
            }
        }
        if (request.getWeight() != null) {
            if (request.getWeight() == 0) {
                dish.setWeight(null);
            } else {
                Validator.positiveInteger("Weight", request.getWeight(), 100000, loggingService); // validate weight
                dish.setWeight(request.getWeight());
            }
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
