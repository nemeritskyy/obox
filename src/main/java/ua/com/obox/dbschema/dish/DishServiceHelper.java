package ua.com.obox.dbschema.dish;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import ua.com.obox.dbschema.tools.logging.LogLevel;
import ua.com.obox.dbschema.tools.logging.LoggingService;
import ua.com.obox.dbschema.tools.services.UpdateServiceHelper;

@Service
public class DishServiceHelper {
    public String getAssociatedIdForDish(String categoryId, DishRepository dishRepository, LoggingService loggingService) {
        String associatedId;
        String restaurantId = dishRepository.findRestaurantIdByCategoryId(categoryId);
        String languageCode = dishRepository.findLanguageCode(categoryId);
        associatedId = dishRepository.findAssociatedIdByRestaurantId(restaurantId, languageCode);
        if (associatedId == null) {
            loggingService.log(LogLevel.ERROR, "Field category_id is required, or you use bad value");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Field category_id is required, or you use bad value");
        }
        System.out.println(associatedId);
        return associatedId;
    }

    public void updateNameIfNeeded(String name, Dish dish, String loggingMessage, LoggingService loggingService, UpdateServiceHelper serviceHelper) {
        if (name != null) {
            serviceHelper.updateNameField(dish::setName, name, "Name", loggingMessage, loggingService);
        }
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

    public void updateStateIfNeeded(String state, Dish dish, String loggingMessage, LoggingService loggingService, UpdateServiceHelper serviceHelper) {
        if (state != null) {
            serviceHelper.updateState(dish::setState, state, "State", loggingMessage, loggingService);
        }
    }
}
