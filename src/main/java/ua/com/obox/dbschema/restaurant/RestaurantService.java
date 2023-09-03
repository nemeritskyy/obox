package ua.com.obox.dbschema.restaurant;

import lombok.RequiredArgsConstructor;
import org.hibernate.Session;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import ua.com.obox.dbschema.tools.RequiredServiceHelper;
import ua.com.obox.dbschema.menu.Menu;
import ua.com.obox.dbschema.menu.MenuRepository;
import ua.com.obox.dbschema.menu.MenuResponse;
import ua.com.obox.dbschema.tenant.Tenant;
import ua.com.obox.dbschema.tenant.TenantRepository;
import ua.com.obox.dbschema.tools.exception.ExceptionTools;
import ua.com.obox.dbschema.tools.exception.Message;
import ua.com.obox.dbschema.tools.logging.LogLevel;
import ua.com.obox.dbschema.tools.logging.LoggingService;
import ua.com.obox.dbschema.tools.response.BadFieldsResponse;
import ua.com.obox.dbschema.tools.response.ResponseErrorMap;
import ua.com.obox.dbschema.tools.services.UpdateServiceHelper;
import ua.com.obox.dbschema.tools.translation.CheckHeader;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RestaurantService{
    @PersistenceContext
    private EntityManager entityManager;
    private final RestaurantRepository restaurantRepository;
    private final TenantRepository tenantRepository;
    private final MenuRepository menuRepository;
    private final LoggingService loggingService;
    private final UpdateServiceHelper serviceHelper;
    private final RequiredServiceHelper requiredServiceHelper;
    private final ResourceBundle translation = ResourceBundle.getBundle("translation.messages");

    public List<MenuResponse> getAllMenusByRestaurantId(String restaurantId, String acceptLanguage) {
        String finalAcceptLanguage = CheckHeader.checkHeaderLanguage(acceptLanguage);

        var restaurantInfo = restaurantRepository.findByRestaurantId(restaurantId);

        restaurantInfo.orElseThrow(() -> {
            ExceptionTools.notFoundResponse(".restaurantNotFound", finalAcceptLanguage, restaurantId);
            return null;
        });

        List<Menu> menus = menuRepository.findAllByRestaurant_RestaurantId(restaurantId);

        List<MenuResponse> responseList = menus.stream()
                .map(menu -> MenuResponse.builder()
                        .menuId(menu.getMenuId())
                        .name(menu.getName())
                        .restaurantId(menu.getRestaurant().getRestaurantId())
                        .language(menu.getLanguage_code())
                        .build()).collect(Collectors.toList());

        loggingService.log(LogLevel.INFO, String.format("getAllMenusByRestaurantId %s %s %d", restaurantId, Message.FIND_COUNT.getMessage(), responseList.size()));
        return responseList;
    }

    public RestaurantResponse getRestaurantById(String restaurantId, String acceptLanguage) {
        String finalAcceptLanguage = CheckHeader.checkHeaderLanguage(acceptLanguage);

        var restaurantInfo = restaurantRepository.findByRestaurantId(restaurantId);

        Restaurant restaurant = restaurantInfo.orElseThrow(() -> {
            ExceptionTools.notFoundResponse(".restaurantNotFound", finalAcceptLanguage, restaurantId);
            return null;
        });

        loggingService.log(LogLevel.INFO, String.format("getRestaurantById %s", restaurantId));
        return RestaurantResponse.builder()
                .restaurantId(restaurant.getRestaurantId())
                .address(restaurant.getAddress())
                .name(restaurant.getName())
                .tenantId(restaurant.getTenant().getTenantId())
                .build();
    }

    public RestaurantResponseId createRestaurant(Restaurant request, String acceptLanguage) {
        String finalAcceptLanguage = CheckHeader.checkHeaderLanguage(acceptLanguage);
        Map<String, String> fieldErrors = new ResponseErrorMap<>();

        request.setTenantIdForRestaurant(request.getTenant_id());

        Tenant tenant = tenantRepository.findByTenantId(request.getTenant().getTenantId()).orElseGet(() -> {
            fieldErrors.put("tenant_id", String.format(translation.getString(finalAcceptLanguage + ".tenantNotFound"), request.getTenant_id()));
            return null;
        });

        Restaurant restaurant = Restaurant.builder()
                .tenant(tenant)
                .build();

        fieldErrors.put("name", serviceHelper.updateNameFieldTranslationSupport(restaurant::setName, request.getName(), finalAcceptLanguage));
        fieldErrors.put("address", serviceHelper.updateVarcharFieldTranslationSupport(restaurant::setAddress, request.getAddress(), "address", finalAcceptLanguage));

        if (fieldErrors.size() > 0)
            throw new BadFieldsResponse(HttpStatus.BAD_REQUEST, fieldErrors);

        restaurantRepository.save(restaurant);

        loggingService.log(LogLevel.INFO, String.format("createRestaurant %s UUID=%s %s", request.getName(), restaurant.getRestaurantId(), Message.CREATE.getMessage()));
        return RestaurantResponseId.builder()
                .restaurantId(restaurant.getRestaurantId())
                .build();
    }

    public void patchRestaurantById(String restaurantId, Restaurant request, String acceptLanguage) {
        String finalAcceptLanguage = CheckHeader.checkHeaderLanguage(acceptLanguage);
        Map<String, String> fieldErrors = new ResponseErrorMap<>();

        try (Session session = entityManager.unwrap(Session.class)) {
            var restaurantInfo = restaurantRepository.findByRestaurantId(restaurantId);

            Restaurant restaurant = restaurantInfo.orElseThrow(() -> {
                ExceptionTools.notFoundResponse(".restaurantNotFound", finalAcceptLanguage, restaurantId);
                return null;
            });

            session.evict(restaurant); // unbind the session

            if (request.getName() != null)
                fieldErrors.put("name", requiredServiceHelper.updateNameIfNeeded(request.getName(), restaurant, finalAcceptLanguage));
            if (request.getAddress() != null)
                fieldErrors.put("address", serviceHelper.updateVarcharFieldTranslationSupport(restaurant::setAddress, request.getAddress(), "address", finalAcceptLanguage));

            if (fieldErrors.size() > 0)
                throw new BadFieldsResponse(HttpStatus.BAD_REQUEST, fieldErrors);

            restaurantRepository.save(restaurant);
        }
        loggingService.log(LogLevel.INFO, String.format("patchRestaurantById %s %s", restaurantId, Message.UPDATE.getMessage()));
    }

    public void deleteRestaurantById(String restaurantId, String acceptLanguage) {
        String finalAcceptLanguage = CheckHeader.checkHeaderLanguage(acceptLanguage);

        var restaurantInfo = restaurantRepository.findByRestaurantId(restaurantId);

        Restaurant restaurant = restaurantInfo.orElseThrow(() -> {
            ExceptionTools.notFoundResponse(".restaurantNotFound", finalAcceptLanguage, restaurantId);
            return null;
        });

        restaurantRepository.delete(restaurant);
        loggingService.log(LogLevel.INFO, String.format("restaurantId %s NAME=%s %s", restaurantId, restaurant.getName(), Message.DELETE.getMessage()));
    }
}