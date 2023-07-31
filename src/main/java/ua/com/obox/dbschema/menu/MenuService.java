package ua.com.obox.dbschema.menu;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import ua.com.obox.dbschema.associateddata.RestaurantAssociatedDataRepository;
import ua.com.obox.dbschema.category.Category;
import ua.com.obox.dbschema.category.CategoryRepository;
import ua.com.obox.dbschema.category.CategoryResponse;
import ua.com.obox.dbschema.restaurant.Restaurant;
import ua.com.obox.dbschema.restaurant.RestaurantRepository;
import ua.com.obox.dbschema.tools.exception.ExceptionTools;
import ua.com.obox.dbschema.tools.exception.Message;
import ua.com.obox.dbschema.tools.logging.LogLevel;
import ua.com.obox.dbschema.tools.logging.LoggingService;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MenuService {
    private final MenuRepository menuRepository;
    private final RestaurantRepository restaurantRepository;
    private final CategoryRepository categoryRepository;
    private final LoggingService loggingService;

    private final RestaurantAssociatedDataRepository dataRepository;
    private String loggingMessage;

    public List<CategoryResponse> getAllCategoriesByMenuId(String menuId) {
        loggingMessage = ExceptionTools.generateLoggingMessage("getAllCategoriesByMenuId", menuId);
        List<Category> categories = categoryRepository.findAllByMenu_MenuId(menuId);
        if (categories.isEmpty()) {
            loggingService.log(LogLevel.ERROR, loggingMessage + Message.NOT_FOUND.getMessage());
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Categories with Menu id " + menuId + Message.NOT_FOUND.getMessage(), null);
        }
        List<CategoryResponse> responseList = new ArrayList<>();

        for (Category category : categories) {
            CategoryResponse response = CategoryResponse.builder()
                    .categoryId(category.getCategoryId())
                    .name(category.getName())
                    .menuId(category.getMenu().getMenuId())
                    .build();
            responseList.add(response);
        }

        loggingService.log(LogLevel.INFO, loggingMessage + Message.FIND_COUNT.getMessage() + responseList.size());
        return responseList;
    }

    public MenuResponse getMenuById(String menuId) {
        loggingMessage = ExceptionTools.generateLoggingMessage("getMenuById", menuId);
        var menuInfo = menuRepository.findByMenuId(menuId);
        Menu menu = menuInfo.orElseThrow(() -> {
            loggingService.log(LogLevel.ERROR, loggingMessage + Message.NOT_FOUND.getMessage());
            return new ResponseStatusException(HttpStatus.NOT_FOUND, "Menu with id " + menuId + Message.NOT_FOUND.getMessage());
        });
        loggingService.log(LogLevel.INFO, loggingMessage);
        return MenuResponse.builder()
                .menuId(menu.getMenuId())
                .name(menu.getName())
                .restaurantId(menu.getRestaurant().getRestaurantId())
                .language(menu.getLanguage_code())
                .build();
    }

    public MenuResponseId createMenu(Menu request) {
        loggingMessage = ExceptionTools.generateLoggingMessage("createMenu", request.getRestaurant_id());
        request.setRestaurantIdForMenu(request.getRestaurant_id(),request.getLanguage_code(), dataRepository);
        Restaurant restaurant = restaurantRepository.findByRestaurantId(request.getRestaurant().getRestaurantId()).orElseThrow(() -> {
            loggingService.log(LogLevel.ERROR, loggingMessage + Message.RESTAURANT_NOT_FOUND.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Restaurant with id " + request.getRestaurant().getRestaurantId() + Message.NOT_FOUND.getMessage(), null);
        });
        Menu menu = Menu.builder()
                .name(request.getName().trim()) // delete whitespaces
                .restaurant(restaurant)
                .language_code(request.getLanguage_code())
                .build();
        menuRepository.save(menu);
        loggingService.log(LogLevel.INFO, loggingMessage + " id=" + menu.getMenuId() + Message.CREATE.getMessage());
        return MenuResponseId.builder()
                .menuId(menu.getMenuId())
                .build();
    }

    public void patchMenuById(String menuId, Menu request) {
        loggingMessage = ExceptionTools.generateLoggingMessage("patchMenuById", menuId);
        var menuInfo = menuRepository.findByMenuId(menuId);
        Menu menu = menuInfo.orElseThrow(() -> {
            loggingService.log(LogLevel.ERROR, loggingMessage + Message.NOT_FOUND.getMessage());
            return new ResponseStatusException(HttpStatus.NOT_FOUND, "Menu with id " + menuId + Message.NOT_FOUND.getMessage());
        });
        String oldName = menu.getName();
        menu.setName(request.getName().trim()); // delete whitespaces
        menuRepository.save(menu);
        loggingService.log(LogLevel.INFO, loggingMessage + " OLD name=" + oldName + " NEW name=" + request.getName() + Message.UPDATE.getMessage());
    }

    public void deleteMenuById(String menuId) {
        loggingMessage = ExceptionTools.generateLoggingMessage("deleteMenuById", menuId);
        var menuInfo = menuRepository.findByMenuId(menuId);
        Menu menu = menuInfo.orElseThrow(() -> {
            loggingService.log(LogLevel.ERROR, loggingMessage + Message.NOT_FOUND.getMessage());
            return new ResponseStatusException(HttpStatus.NOT_FOUND, "Menu with id " + menuId + Message.NOT_FOUND.getMessage());
        });
        menuRepository.delete(menu);
        loggingService.log(LogLevel.INFO, loggingMessage + " name=" + menu.getName() + Message.DELETE.getMessage());
    }
}
