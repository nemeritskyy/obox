package ua.com.obox.dbschema.tools;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ua.com.obox.dbschema.category.Category;
import ua.com.obox.dbschema.dish.Dish;
import ua.com.obox.dbschema.menu.Menu;
import ua.com.obox.dbschema.restaurant.Restaurant;
import ua.com.obox.dbschema.tenant.Tenant;
import ua.com.obox.dbschema.tools.configuration.ValidationConfiguration;
import ua.com.obox.dbschema.tools.logging.LoggingService;
import ua.com.obox.dbschema.tools.services.UpdateServiceHelper;

import java.util.ResourceBundle;

@Service
public class RequiredServiceHelper {
    @Autowired
    LoggingService loggingService;
    @Autowired
    UpdateServiceHelper serviceHelper;

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

    public String updatePriceIfNeeded(Double price, Dish dish, boolean zeroApply, String acceptLanguage) {
        if (price != null && zeroApply) {
            return serviceHelper.updatePriceField(dish::setSpecialPrice, price, ValidationConfiguration.MAX_PRICE, "specialPrice", zeroApply, acceptLanguage);
        } else if (price != null) {
            return serviceHelper.updatePriceField(dish::setPrice, price, ValidationConfiguration.MAX_PRICE, "price", zeroApply, acceptLanguage);
        }
        return null;
    }
}
