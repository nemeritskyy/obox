package ua.com.obox.dbschema.menu;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import ua.com.obox.dbschema.restaurant.Restaurant;
import ua.com.obox.dbschema.restaurant.RestaurantRepository;
import ua.com.obox.dbschema.tools.exception.ExceptionTools;
import ua.com.obox.dbschema.tools.exception.Message;
import ua.com.obox.dbschema.tools.logging.LogLevel;
import ua.com.obox.dbschema.tools.logging.LoggingService;

@Service
@RequiredArgsConstructor
public class MenuService {
    private final MenuRepository menuRepository;
    private final RestaurantRepository restaurantRepository;
    private final LoggingService loggingService;
    private String loggingMessage;

    public MenuResponse getMenuById(String menuId) {
        loggingMessage = ExceptionTools.generateLoggingMessage("getMenuById", menuId);
        var tenantInfo = menuRepository.findByMenuId(menuId);
        Menu menu = tenantInfo.orElseThrow(() -> {
            loggingService.log(LogLevel.ERROR, loggingMessage + Message.NOT_FOUND.getMessage());
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, Message.NOT_FOUND.getMessage().trim(), null);
        });
        loggingService.log(LogLevel.INFO, loggingMessage + Message.GET_BY_ID.getMessage());
        return MenuResponse.builder()
                .menuId(menu.getMenuId())
                .name(menu.getName())
                .restaurantId(menu.getRestaurant().getRestaurantId())
                .build();
    }

    public MenuResponseId createMenu(Menu request) {
        loggingMessage = ExceptionTools.generateLoggingMessage("createMenu", request.toString());
        request.setRestaurantIdForMenu(request.getRestaurant_id());
        Restaurant restaurant = restaurantRepository.findByRestaurantId(request.getRestaurant().getRestaurantId()).orElseThrow(() -> {
            loggingService.log(LogLevel.ERROR, loggingMessage + Message.RESTAURANT_NOT_FOUND.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, Message.RESTAURANT_NOT_FOUND.getMessage().trim(), null);
        });
        Menu menu = Menu.builder()
                .name(request.getName().trim()) // delete whitespaces
                .restaurant(restaurant)
                .build();
        menuRepository.save(menu);
        loggingService.log(LogLevel.INFO, loggingMessage + Message.CREATE.getMessage());
        return MenuResponseId.builder()
                .menuId(menu.getMenuId())
                .build();
    }

    public void patchMenuById(String menuId, Menu request) {
        loggingMessage = ExceptionTools.generateLoggingMessage("patchMenuById", menuId);
        var menuInfo = menuRepository.findByMenuId(menuId);
        Menu menu = menuInfo.orElseThrow(() -> {
            loggingService.log(LogLevel.ERROR, loggingMessage + Message.NOT_FOUND.getMessage());
            return new ResponseStatusException(HttpStatus.NOT_FOUND, Message.NOT_FOUND.getMessage().trim());
        });
        String oldName = menu.getName();
        menu.setName(request.getName().trim()); // delete whitespaces
        menuRepository.save(menu);
        loggingService.log(LogLevel.INFO, loggingMessage + " OLD name=" + oldName + " NEW" + Message.UPDATE.getMessage());
    }

    public void deleteMenuById(String menuId) {
        loggingMessage = ExceptionTools.generateLoggingMessage("deleteMenuById", menuId);
        var menuInfo = menuRepository.findByMenuId(menuId);
        Menu menu = menuInfo.orElseThrow(() -> {
            loggingService.log(LogLevel.ERROR, loggingMessage + Message.MENU_NOT_FOUND.getMessage());
            return new ResponseStatusException(HttpStatus.NOT_FOUND, Message.MENU_NOT_FOUND.getMessage().trim());
        });
        menuRepository.delete(menu);
        loggingService.log(LogLevel.INFO, loggingMessage + Message.DELETE.getMessage() + menu.getName());
    }
}
