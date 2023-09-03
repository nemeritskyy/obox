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
import ua.com.obox.dbschema.tools.Validator;
import ua.com.obox.dbschema.tools.exception.ExceptionTools;
import ua.com.obox.dbschema.tools.response.BadFieldsResponse;
import ua.com.obox.dbschema.tools.response.ResponseErrorMap;
import ua.com.obox.dbschema.tools.services.UpdateServiceHelper;
import ua.com.obox.dbschema.tools.exception.Message;
import ua.com.obox.dbschema.tools.logging.LogLevel;
import ua.com.obox.dbschema.tools.logging.LoggingService;
import ua.com.obox.dbschema.tools.translation.CheckHeader;

import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MenuService {
    private final MenuRepository menuRepository;
    private final RestaurantRepository restaurantRepository;
    private final CategoryRepository categoryRepository;
    private final LoggingService loggingService;
    private final RestaurantAssociatedDataRepository dataRepository;
    private final UpdateServiceHelper serviceHelper;
    private final RequiredServiceHelper requiredServiceHelper;
    private final ResourceBundle translation = ResourceBundle.getBundle("translation.messages");

    public List<CategoryResponse> getAllCategoriesByMenuId(String menuId, String acceptLanguage) {
        String finalAcceptLanguage = CheckHeader.checkHeaderLanguage(acceptLanguage);

        var menuInfo = menuRepository.findByMenuId(menuId);

        menuInfo.orElseThrow(() -> {
            ExceptionTools.notFoundResponse(".menuNotFound", finalAcceptLanguage, menuId);
            return null;
        });

        List<Category> categories = categoryRepository.findAllByMenu_MenuId(menuId);

        List<CategoryResponse> responseList = categories.stream()
                .map(category -> CategoryResponse.builder()
                        .categoryId(category.getCategoryId())
                        .name(category.getName())
                        .menuId(category.getMenu().getMenuId())
                        .build()).collect(Collectors.toList());

        loggingService.log(LogLevel.INFO, String.format("getAllCategoriesByMenuId %s %s %d", menuId, Message.FIND_COUNT.getMessage(), responseList.size()));
        return responseList;
    }

    public MenuResponse getMenuById(String menuId, String acceptLanguage) {
        String finalAcceptLanguage = CheckHeader.checkHeaderLanguage(acceptLanguage);

        var menuInfo = menuRepository.findByMenuId(menuId);

        Menu menu = menuInfo.orElseThrow(() -> {
            ExceptionTools.notFoundResponse(".menuNotFound", finalAcceptLanguage, menuId);
            return null;
        });

        loggingService.log(LogLevel.INFO, String.format("getMenuById %s", menuId));
        return MenuResponse.builder()
                .menuId(menu.getMenuId())
                .name(menu.getName())
                .restaurantId(menu.getRestaurant().getRestaurantId())
                .language(menu.getLanguage_code())
                .build();
    }

    public MenuResponseId createMenu(Menu request, String acceptLanguage) {
        String finalAcceptLanguage = CheckHeader.checkHeaderLanguage(acceptLanguage);
        Map<String, String> fieldErrors = new ResponseErrorMap<>();

        Restaurant restaurant = restaurantRepository.findByRestaurantId(request.getRestaurant_id())
                .orElseGet(() -> {
                    fieldErrors.put("restaurant_id", String.format(translation.getString(finalAcceptLanguage + ".restaurantNotFound"), request.getRestaurant_id()));
                    return null;
                });

        Menu menu = Menu.builder()
                .restaurant(restaurant)
                .build();

        if (request.getName() != null && !menuRepository.findAllByRestaurant_RestaurantIdAndName(request.getRestaurant_id(), Validator.removeExtraSpaces(request.getName())).isEmpty()) {
            fieldErrors.put("name", translation.getString(finalAcceptLanguage + ".menuExists"));
        } else {
            fieldErrors.put("name", serviceHelper.updateNameFieldTranslationSupport(menu::setName, request.getName(), finalAcceptLanguage));
        }

        fieldErrors.put("language_code", serviceHelper.updateLanguageCode(menu::setLanguage_code, request.getLanguage_code(), finalAcceptLanguage));


        if (fieldErrors.size() > 0)
            throw new BadFieldsResponse(HttpStatus.BAD_REQUEST, fieldErrors);

        menu.checkAssociatedData(request.getRestaurant_id(), request.getLanguage_code(), dataRepository); // if associated data is empty create it

        menuRepository.save(menu);

        loggingService.log(LogLevel.INFO, String.format("createMenu %s UUID=%s %s", request.getName(), menu.getMenuId(), Message.CREATE.getMessage()));

        return MenuResponseId.builder()
                .menuId(menu.getMenuId())
                .build();
    }

    public void patchMenuById(String menuId, Menu request, String acceptLanguage) {
        String finalAcceptLanguage = CheckHeader.checkHeaderLanguage(acceptLanguage);
        Map<String, String> fieldErrors = new ResponseErrorMap<>();

        var menuInfo = menuRepository.findByMenuId(menuId);

        Menu menu = menuInfo.orElseThrow(() -> {
            ExceptionTools.notFoundResponse(".menuNotFound", finalAcceptLanguage, menuId);
            return null;
        });

        if (request.getName() != null && !menu.getName().equals(Validator.removeExtraSpaces(request.getName()))) {
            if (!menuRepository.findAllByRestaurant_RestaurantIdAndName(menu.getRestaurant().getRestaurantId(), Validator.removeExtraSpaces(request.getName())).isEmpty()) {
                fieldErrors.put("name", translation.getString(finalAcceptLanguage + ".menuExists"));
            } else {
                fieldErrors.put("name", requiredServiceHelper.updateNameIfNeeded(request.getName(), menu, finalAcceptLanguage));
            }
        }

        if (fieldErrors.size() > 0)
            throw new BadFieldsResponse(HttpStatus.BAD_REQUEST, fieldErrors);

        menuRepository.save(menu);
        loggingService.log(LogLevel.INFO, String.format("patchMenuById %s %s", menuId, Message.UPDATE.getMessage()));
    }

    public void deleteMenuById(String menuId, String acceptLanguage) {
        String finalAcceptLanguage = CheckHeader.checkHeaderLanguage(acceptLanguage);

        var menuInfo = menuRepository.findByMenuId(menuId);
        Menu menu = menuInfo.orElseThrow(() -> {
            ExceptionTools.notFoundResponse(".menuNotFound", finalAcceptLanguage, menuId);
            return null;
        });

        menuRepository.delete(menu);
        loggingService.log(LogLevel.INFO, String.format("deleteMenuById %s NAME=%s %s", menuId, menu.getName(), Message.DELETE.getMessage()));
    }
}