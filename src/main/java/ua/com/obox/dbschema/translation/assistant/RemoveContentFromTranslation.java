package ua.com.obox.dbschema.translation.assistant;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ua.com.obox.dbschema.category.Category;
import ua.com.obox.dbschema.dish.Dish;
import ua.com.obox.dbschema.language.LanguageRepository;
import ua.com.obox.dbschema.menu.Menu;
import ua.com.obox.dbschema.restaurant.Restaurant;
import ua.com.obox.dbschema.restaurant.RestaurantRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RemoveContentFromTranslation {
    private final LanguageRepository languageRepository;
    private final RestaurantRepository restaurantRepository;

    public void removeContentByRestaurantId(String restaurantId, List<String> removedLanguagesId) {
        Optional<Restaurant> restaurant = restaurantRepository.findByRestaurantId(restaurantId);
        List<String> restaurantConnections = new ArrayList<>();
        if (restaurant.isPresent()) {
            for (Menu menu : restaurant.get().getMenus()) {
                restaurantConnections.add(menu.getMenuId());
                for (Category category : menu.getCategories()) {
                    restaurantConnections.add(category.getCategoryId());
                    for (Dish dish : category.getDishes()) {
                        restaurantConnections.add(dish.getDishId());
                    }
                }
            }
        }
        for (String s : restaurantConnections) {
            System.out.println("translation id " + s);
        }
    }
}
