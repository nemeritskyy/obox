package ua.com.obox.dbschema.menu;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import ua.com.obox.dbschema.associateddata.RestaurantAssociatedDataRepository;
import ua.com.obox.dbschema.category.Category;
import ua.com.obox.dbschema.category.CategoryRepository;
import ua.com.obox.dbschema.category.CategoryResponse;
import ua.com.obox.dbschema.restaurant.Restaurant;
import ua.com.obox.dbschema.restaurant.RestaurantRepository;
import ua.com.obox.dbschema.tools.RequiredServiceHelper;
import ua.com.obox.dbschema.tools.response.BadFieldsResponse;
import ua.com.obox.dbschema.tools.response.ResponseErrorMap;
import ua.com.obox.dbschema.tools.services.UpdateServiceHelper;
import ua.com.obox.dbschema.tools.exception.Message;
import ua.com.obox.dbschema.tools.logging.LogLevel;
import ua.com.obox.dbschema.tools.logging.LoggingService;
import ua.com.obox.dbschema.tools.services.AbstractResponseService;
import ua.com.obox.dbschema.tools.services.LoggingResponseHelper;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MenuService extends AbstractResponseService {
    private final MenuRepository menuRepository;
    private final RestaurantRepository restaurantRepository;
    private final CategoryRepository categoryRepository;
    private final LoggingService loggingService;
    private final RestaurantAssociatedDataRepository dataRepository;
    private final UpdateServiceHelper serviceHelper;
    private final RequiredServiceHelper requiredServiceHelper;
    private String loggingMessage;
    private String responseMessage;

    public List<CategoryResponse> getAllCategoriesByMenuId(String menuId) {
        Menu menu;
        loggingMessage = "getAllCategoriesByMenuId";
        responseMessage = String.format("Categories with Menu id %s", menuId);

        var menuInfo = menuRepository.findByMenuId(menuId);

        menu = menuInfo.orElseThrow(() -> {
            notFoundResponse(menuId);
            return null;
        });

        List<Category> categories = categoryRepository.findAllByMenu_MenuId(menuId);

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
        Map<String, String> fieldErrors = new ResponseErrorMap<>();
        loggingMessage = "createMenu";
        responseMessage = String.format("Restaurant with id %s", request.getRestaurant_id());

        restaurant = restaurantRepository.findByRestaurantId(request.getRestaurant_id())
                .orElseGet(() -> {
                    fieldErrors.put("restaurant_id", responseMessage + Message.NOT_FOUND.getMessage());
                    return null;
                });

        menu = Menu.builder()
                .restaurant(restaurant)
                .build();

        if (request.getName() != null && !menuRepository.findAllByRestaurant_RestaurantIdAndName(request.getRestaurant_id(), request.getName().trim()).isEmpty()) {
            loggingMessage = Message.MENU_EXISTS.getMessage();
            fieldErrors.put("name", Message.MENU_EXISTS.getMessage());
        } else {
            fieldErrors.put("name", serviceHelper.updateNameField(menu::setName, request.getName(), "Name", loggingMessage));
        }

        fieldErrors.put("language_code", serviceHelper.updateLanguageCode(menu::setLanguage_code, request.getLanguage_code()));


        if (fieldErrors.size() > 0)
            throw new BadFieldsResponse(HttpStatus.BAD_REQUEST, fieldErrors);

        menu.checkAssociatedData(request.getRestaurant_id(), request.getLanguage_code(), dataRepository); // if associated data is empty create it

        menuRepository.save(menu);

        loggingService.log(LogLevel.INFO, String.format("%s %s UUID=%s %s", loggingMessage, request.getName(), menu.getMenuId(), Message.CREATE.getMessage()));

        return MenuResponseId.builder()
                .menuId(menu.getMenuId())
                .build();
    }

    public void patchMenuById(String menuId, Menu request) {
        Menu menu;
        Map<String, String> fieldErrors = new ResponseErrorMap<>();

        loggingMessage = "patchMenuById";
        responseMessage = String.format("Menu with id %s", menuId);
        var menuInfo = menuRepository.findByMenuId(menuId);

        menu = menuInfo.orElseThrow(() -> {
            notFoundResponse(menuId);
            return null;
        });

        if (request.getName() != null) {
            if (!menuRepository.findAllByRestaurant_RestaurantIdAndName(menu.getRestaurant().getRestaurantId(), request.getName().trim()).isEmpty()) {
                loggingMessage = Message.MENU_EXISTS.getMessage();
                fieldErrors.put("name", Message.MENU_EXISTS.getMessage());
            } else {
                fieldErrors.put("name", requiredServiceHelper.updateNameIfNeeded(request.getName(), menu, loggingMessage));
            }
        }

        if (fieldErrors.size() > 0)
            throw new BadFieldsResponse(HttpStatus.BAD_REQUEST, fieldErrors);

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
        loggingService.log(LogLevel.INFO, String.format("%s %s NAME=%s %s", loggingMessage, menuId, menu.getName(), Message.DELETE.getMessage()));
    }

    @Override
    public void notFoundResponse(String entityId) {
        LoggingResponseHelper.loggingThrowException(
                entityId,
                LogLevel.ERROR, HttpStatus.NOT_FOUND,
                loggingMessage, responseMessage + Message.NOT_FOUND.getMessage(),
                loggingService);
    }
}
