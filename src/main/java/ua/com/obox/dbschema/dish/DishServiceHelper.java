package ua.com.obox.dbschema.dish;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import ua.com.obox.dbschema.tools.Validator;
import ua.com.obox.dbschema.tools.logging.LogLevel;
import ua.com.obox.dbschema.tools.logging.LoggingService;

import java.util.function.Consumer;

@Service
public class DishServiceHelper {
    public String getAssociatedIdForDish(String categoryId, DishRepository dishRepository, LoggingService loggingService) {
        String associatedId = null;
        String restaurantId = dishRepository.findRestaurantIdByCategoryId(categoryId);
        String languageCode = dishRepository.findLanguageCode(categoryId);
        associatedId = dishRepository.findAssociatedIdByRestaurantId(restaurantId, languageCode);
        if (associatedId == null) {
            loggingService.log(LogLevel.ERROR, "Wrong associated query, category_id is required");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Wrong associated query, category_id is required");
        }
        System.out.println(associatedId);
        return associatedId;
    }

    public void updateDishFromRequestNullEnable(Dish dish, Dish request, String loggingMessage, LoggingService loggingService) {
        updateStringField(dish::setDescription, request.getDescription(), "Description", loggingMessage, loggingService);
        updateStringField(dish::setAllergens, request.getAllergens(), "Allergens", loggingMessage, loggingService);
        updateStringField(dish::setTags, request.getTags(), "Tags", loggingMessage, loggingService);
        updateIntegerField(dish::setCalories, request.getCalories(), "Calories", loggingMessage, loggingService, 0, 30000);
        updateIntegerField(dish::setWeight, request.getWeight(), "Weight", loggingMessage, loggingService, 0, 100000);
    }

    public void updateStringField(Consumer<String> setter, String value, String field, String loggingMessage, LoggingService loggingService) {
        if (value != null) {
            String trimmedValue = value.trim();
            if (!trimmedValue.isEmpty()) {
                Validator.validateVarchar(loggingMessage, field, trimmedValue, loggingService);
                setter.accept(trimmedValue);
            } else {
                setter.accept(null);
            }
        }
    }

    private void updateIntegerField(Consumer<Integer> setter, Integer value, String field, String loggingMessage, LoggingService loggingService, int minValue, int maxValue) {
        if (value != null) {
            if (value == 0) {
                setter.accept(null);
            } else {
                Validator.positiveInteger(field, value, maxValue, loggingService);
                setter.accept(value);
            }
        }
    }
}
