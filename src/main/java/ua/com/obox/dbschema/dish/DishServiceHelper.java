package ua.com.obox.dbschema.dish;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import ua.com.obox.dbschema.tools.exception.Message;
import ua.com.obox.dbschema.tools.logging.LogLevel;
import ua.com.obox.dbschema.tools.logging.LoggingService;

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
}
