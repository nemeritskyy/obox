package ua.com.obox.dbschema.tools;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import ua.com.obox.dbschema.category.Category;
import ua.com.obox.dbschema.dish.Dish;
import ua.com.obox.dbschema.dish.DishRepository;
import ua.com.obox.dbschema.menu.Menu;
import ua.com.obox.dbschema.restaurant.Restaurant;
import ua.com.obox.dbschema.tenant.Tenant;
import ua.com.obox.dbschema.tools.logging.LogLevel;
import ua.com.obox.dbschema.tools.logging.LoggingService;
import ua.com.obox.dbschema.tools.services.UpdateServiceHelper;

@Service
public class RequiredServiceHelper {
    @Autowired
    LoggingService loggingService;
    @Autowired
    UpdateServiceHelper serviceHelper;
    public String getAssociatedIdForDish(String categoryId, DishRepository dishRepository) {
        String restaurantId = dishRepository.findRestaurantIdByCategoryId(categoryId);
        String languageCode = dishRepository.findLanguageCode(categoryId);
        String associatedId = dishRepository.findAssociatedIdByRestaurantId(restaurantId, languageCode);
        if (associatedId == null) {
            loggingService.log(LogLevel.ERROR, "Field category_id is required, or you use bad value");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You use bad data without associated");
        }
        System.out.println(associatedId);
        return associatedId;
    }

    public String updateNameIfNeeded(String name, Dish dish, String loggingMessage) {
        if (name != null) {
            return serviceHelper.updateNameField(dish::setName, name, "Name", loggingMessage);
        }
        return null;
    }

    public String updateNameIfNeeded(String name, Tenant tenant, String loggingMessage) {
        if (name != null) {
            return serviceHelper.updateNameField(tenant::setName, name, "Name", loggingMessage);
        }
        return null;
    }

    public String updateNameIfNeeded(String name, Restaurant restaurant, String loggingMessage) {
        if (name != null) {
            return serviceHelper.updateNameField(restaurant::setName, name, "Name", loggingMessage);
        }
        return null;
    }

    public String updateNameIfNeeded(String name, Menu menu, String loggingMessage) {
        if (name != null) {
            return serviceHelper.updateNameField(menu::setName, name, "Name", loggingMessage);
        }
        return null;
    }

    public String updateNameIfNeeded(String name, Category category, String loggingMessage) {
        if (name != null) {
            return serviceHelper.updateNameField(category::setName, name, "Name", loggingMessage);
        }
        return null;
    }

    public String updatePriceIfNeeded(Double price, Dish dish, String loggingMessage) {
        if (price != null) {
            return serviceHelper.updatePriceField(dish::setPrice, price, "Price", loggingMessage, 100_000);
        }
        return null;
    }

    public void updateStateIfNeeded(String state, Dish dish, String loggingMessage) {
        if (state != null) {
            serviceHelper.updateState(dish::setState, state, "State", loggingMessage);
        }
    }
}
