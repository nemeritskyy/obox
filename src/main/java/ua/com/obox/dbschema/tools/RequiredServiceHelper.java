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
import ua.com.obox.dbschema.tools.configuration.ValidationConfiguration;
import ua.com.obox.dbschema.tools.logging.LogLevel;
import ua.com.obox.dbschema.tools.logging.LoggingService;
import ua.com.obox.dbschema.tools.services.UpdateServiceHelper;

import java.util.ResourceBundle;

@Service
public class RequiredServiceHelper {
    @Autowired
    LoggingService loggingService;
    @Autowired
    UpdateServiceHelper serviceHelper;

    private final ResourceBundle translation = ResourceBundle.getBundle("translation.messages");

    public String getAssociatedIdForDish(String categoryId, DishRepository dishRepository, String acceptLanguage) {
        String restaurantId = dishRepository.findRestaurantIdByCategoryId(categoryId);
        String languageCode = dishRepository.findLanguageCode(categoryId);
        String associatedId = dishRepository.findAssociatedIdByRestaurantId(restaurantId, languageCode);
        if (associatedId == null) {
            loggingService.log(LogLevel.ERROR, translation.getString("en-US.badAssociated"));
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, translation.getString(acceptLanguage + ".badAssociated"));
        }
        return associatedId;
    }

    public String updateNameIfNeeded(String name, Dish dish, String acceptLanguage) {
        if (name != null) {
            return serviceHelper.updateNameField(dish::setName, name, acceptLanguage);
        }
        return null;
    }

    public String updateNameIfNeeded(String name, Tenant tenant, String acceptLanguage) {
        if (name != null) {
            return serviceHelper.updateNameField(tenant::setName, name, acceptLanguage);
        }
        return null;
    }

    public String updateNameIfNeeded(String name, Restaurant restaurant, String acceptLanguage) {
        if (name != null) {
            return serviceHelper.updateNameField(restaurant::setName, name, acceptLanguage);
        }
        return null;
    }

    public String updateNameIfNeeded(String name, Menu menu, String acceptLanguage) {
        if (name != null) {
            return serviceHelper.updateNameField(menu::setName, name, acceptLanguage);
        }
        return null;
    }

    public String updateNameIfNeeded(String name, Category category, String acceptLanguage) {
        if (name != null) {
            return serviceHelper.updateNameField(category::setName, name, acceptLanguage);
        }
        return null;
    }

    public String updatePriceIfNeeded(Double price, Dish dish, String acceptLanguage) {
        if (price != null) {
            return serviceHelper.updatePriceField(dish::setPrice, price, ValidationConfiguration.MAX_PRICE, "price", acceptLanguage);
        }
        return null;
    }
}
