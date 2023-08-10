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
import ua.com.obox.dbschema.restaurant.RestaurantServiceHelper;
import ua.com.obox.dbschema.tools.exception.Message;
import ua.com.obox.dbschema.tools.logging.LogLevel;
import ua.com.obox.dbschema.tools.logging.LoggingService;
import ua.com.obox.dbschema.tools.services.AbstractResponseService;
import ua.com.obox.dbschema.tools.services.LoggingResponseHelper;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MenuService extends AbstractResponseService {
    private final MenuRepository menuRepository;
    private final RestaurantRepository restaurantRepository;
    private final CategoryRepository categoryRepository;
    private final LoggingService loggingService;
    private final RestaurantAssociatedDataRepository dataRepository;
    private final RestaurantServiceHelper serviceHelper;
    private String loggingMessage;
    private String responseMessage;

    public List<CategoryResponse> getAllCategoriesByMenuId(String menuId) {
        loggingMessage = "getAllCategoriesByMenuId";
        responseMessage = String.format("Categories with Menu id %s", menuId);

        List<Category> categories = categoryRepository.findAllByMenu_MenuId(menuId);
        if (categories.isEmpty()) {
            notFoundResponse(menuId);
        }

        List<CategoryResponse> responseList = categories.stream()
                .map(category -> CategoryResponse.builder()
                        .categoryId(category.getCategoryId())
                        .name(category.getName())
                        .menuId(category.getMenu().getMenuId())
                        .build()).collect(Collectors.toList());

        loggingService.log(LogLevel.INFO, String.format("%s %s %s %d", loggingMessage, menuId, Message.FIND_COUNT.getMessage(), responseList.size()));
        return responseList;
    }

    public MenuResponse getMenuById(String menuId) {
        Menu menu;
        loggingMessage = "getMenuById";
        responseMessage = String.format("Menu with id %s", menuId);
        var menuInfo = menuRepository.findByMenuId(menuId);

        menu = menuInfo.orElseThrow(() -> {
            notFoundResponse(menuId);
            return null;
        });

        loggingService.log(LogLevel.INFO, String.format("%s %s", loggingMessage, menuId));
        return MenuResponse.builder()
                .menuId(menu.getMenuId())
                .name(menu.getName())
                .restaurantId(menu.getRestaurant().getRestaurantId())
                .language(menu.getLanguage_code())
                .build();
    }

    public MenuResponseId createMenu(Menu request) {
        Restaurant restaurant;
        Menu menu;
        loggingMessage = "createMenu";
        responseMessage = String.format("Restaurant with id %s", request.getRestaurant_id());

        try {
            restaurant = restaurantRepository.findByRestaurantId(request.getRestaurant_id())
                    .orElseThrow(() -> {
                        badRequestResponse(request.getRestaurant_id());
                        return null;
                    });
        } catch (NullPointerException e) {
            loggingService.log(LogLevel.ERROR, "NullPointerException occurred: " + e.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Null value encountered", e);
        }

        request.checkAssociatedData(request.getRestaurant_id(), request.getLanguage_code(), dataRepository); // if associated data is empty create it

        menu = Menu.builder()
                .name(request.getName().trim())
                .restaurant(restaurant)
                .language_code(request.getLanguage_code().toLowerCase())
                .build();

        menuRepository.save(menu);

        loggingService.log(LogLevel.INFO, String.format("%s %s UUID=%s %s", loggingMessage, request.getName(), menu.getMenuId(), Message.CREATE.getMessage()));

        return MenuResponseId.builder()
                .menuId(menu.getMenuId())
                .build();
    }

    public void patchMenuById(String menuId, Menu request) {
        Menu menu;
        loggingMessage = "patchMenuById";
        responseMessage = String.format("Menu with id %s", menuId);
        var menuInfo = menuRepository.findByMenuId(menuId);

        menu = menuInfo.orElseThrow(() -> {
            notFoundResponse(menuId);
            return null;
        });

        serviceHelper.updateNameField(menu::setName, request.getName(), "Name", loggingMessage, loggingService);

        menuRepository.save(menu);
        loggingService.log(LogLevel.INFO, String.format("%s %s %s", loggingMessage, menuId, Message.UPDATE.getMessage()));
    }

    public void deleteMenuById(String menuId) {
        Menu menu;
        loggingMessage = "deleteMenuById";
        responseMessage = String.format("Menu with id %s", menuId);

        var menuInfo = menuRepository.findByMenuId(menuId);
        menu = menuInfo.orElseThrow(() -> {
            notFoundResponse(menuId);
            return null;
        });

        menuRepository.delete(menu);
        loggingService.log(LogLevel.INFO, String.format("%s %s NAME=%s %s", loggingService, menuId, menu.getName(), Message.DELETE.getMessage()));
    }

    @Override
    public void notFoundResponse(String entityId) {
        LoggingResponseHelper.loggingThrowException(
                entityId,
                LogLevel.ERROR, HttpStatus.NOT_FOUND,
                loggingMessage, responseMessage + Message.NOT_FOUND.getMessage(),
                loggingService);
    }

    @Override
    public void badRequestResponse(String entityId) {
        LoggingResponseHelper.loggingThrowException(
                entityId,
                LogLevel.ERROR, HttpStatus.BAD_REQUEST,
                loggingMessage, responseMessage + Message.NOT_FOUND.getMessage(),
                loggingService);
    }
}
