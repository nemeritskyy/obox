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
import ua.com.obox.dbschema.tools.exception.Message;
import ua.com.obox.dbschema.tools.logging.LogLevel;
import ua.com.obox.dbschema.tools.logging.LoggingService;
import ua.com.obox.dbschema.tools.response.BadFieldsResponse;
import ua.com.obox.dbschema.tools.response.ResponseErrorMap;
import ua.com.obox.dbschema.tools.services.AbstractResponseService;
import ua.com.obox.dbschema.tools.services.LoggingResponseHelper;
import ua.com.obox.dbschema.tools.services.UpdateServiceHelper;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RestaurantService extends AbstractResponseService {
    @PersistenceContext
    private EntityManager entityManager;
    private final RestaurantRepository restaurantRepository;
    private final TenantRepository tenantRepository;
    private final MenuRepository menuRepository;
    private final LoggingService loggingService;
    private final UpdateServiceHelper serviceHelper;
    private final RequiredServiceHelper requiredServiceHelper;
    private String loggingMessage;
    private String responseMessage;

    public List<MenuResponse> getAllMenusByRestaurantId(String restaurantId) {
        loggingMessage = "getAllMenusByRestaurantId";
        responseMessage = String.format("Menus with Restaurant id %s", restaurantId);

        List<Menu> menus = menuRepository.findAllByRestaurant_RestaurantId(restaurantId);
        if (menus.isEmpty()) {
            notFoundResponse(restaurantId);
        }

        List<MenuResponse> responseList = menus.stream()
                .map(menu -> MenuResponse.builder()
                        .menuId(menu.getMenuId())
                        .name(menu.getName())
                        .restaurantId(menu.getRestaurant().getRestaurantId())
                        .language(menu.getLanguage_code())
                        .build()).collect(Collectors.toList());

        loggingService.log(LogLevel.INFO, String.format("%s %s %s %d", loggingMessage, restaurantId, Message.FIND_COUNT.getMessage(), responseList.size()));
        return responseList;
    }

    public RestaurantResponse getRestaurantById(String restaurantId) {
        Restaurant restaurant;
        loggingMessage = "getRestaurantById";
        responseMessage = String.format("Restaurant with id %s", restaurantId);
        var restaurantInfo = restaurantRepository.findByRestaurantId(restaurantId);

        restaurant = restaurantInfo.orElseThrow(() -> {
            notFoundResponse(restaurantId);
            return null;
        });

        loggingService.log(LogLevel.INFO, String.format("%s %s", loggingMessage, restaurantId));
        return RestaurantResponse.builder()
                .restaurantId(restaurant.getRestaurantId())
                .address(restaurant.getAddress())
                .name(restaurant.getName())
                .tenantId(restaurant.getTenant().getTenantId())
                .build();
    }

    public RestaurantResponseId createRestaurant(Restaurant request) {
        Tenant tenant;
        Map<String, String> fieldErrors = new ResponseErrorMap<>();
        Restaurant restaurant;
        loggingMessage = "createRestaurant";
        responseMessage = String.format("Tenant with id %s", request.getTenant_id());

        request.setTenantIdForRestaurant(request.getTenant_id());

        tenant = tenantRepository.findByTenantId(request.getTenant().getTenantId()).orElseGet(() -> {
            fieldErrors.put("tenant_id", responseMessage + Message.NOT_FOUND.getMessage());
            return null;
        });

        restaurant = Restaurant.builder()
                .tenant(tenant)
                .build();

        fieldErrors.put("name", serviceHelper.updateNameField(restaurant::setName, request.getName(), "Name", loggingMessage, loggingService));
        fieldErrors.put("address", serviceHelper.updateVarcharField(restaurant::setAddress, request.getAddress(), "Address", loggingMessage, loggingService));

        if (fieldErrors.size() > 0)
            throw new BadFieldsResponse(HttpStatus.BAD_REQUEST, fieldErrors);

        restaurantRepository.save(restaurant);

        loggingService.log(LogLevel.INFO, String.format("%s %s UUID=%s %s", loggingMessage, request.getName(), restaurant.getRestaurantId(), Message.CREATE.getMessage()));
        return RestaurantResponseId.builder()
                .restaurantId(restaurant.getRestaurantId())
                .build();
    }

    public void patchRestaurantById(String restaurantId, Restaurant request) {
        Restaurant restaurant;
        Map<String, String> fieldErrors = new ResponseErrorMap<>();
        try (Session session = entityManager.unwrap(Session.class)) {
            loggingMessage = "patchRestaurantById";
            responseMessage = String.format("Restaurant with id %s", restaurantId);
            var restaurantInfo = restaurantRepository.findByRestaurantId(restaurantId);

            restaurant = restaurantInfo.orElseThrow(() -> {
                notFoundResponse(restaurantId);
                return null;
            });

            session.evict(restaurant); // unbind the session
        }

        if (request.getName() != null)
            fieldErrors.put("name", requiredServiceHelper.updateNameIfNeeded(request.getName(), restaurant, loggingMessage, loggingService, serviceHelper));
        if (request.getAddress() != null)
            fieldErrors.put("address", serviceHelper.updateVarcharField(restaurant::setAddress, request.getAddress(), "Address", loggingMessage, loggingService));

        if (fieldErrors.size() > 0)
            throw new BadFieldsResponse(HttpStatus.BAD_REQUEST, fieldErrors);

        restaurantRepository.save(restaurant);
        loggingService.log(LogLevel.INFO, String.format("%s %s %s", loggingMessage, restaurantId, Message.UPDATE.getMessage()));
    }

    public void deleteRestaurantById(String restaurantId) {
        Restaurant restaurant;
        loggingMessage = "restaurantId";
        responseMessage = String.format("Restaurant with id %s", restaurantId);
        var restaurantInfo = restaurantRepository.findByRestaurantId(restaurantId);

        restaurant = restaurantInfo.orElseThrow(() -> {
            notFoundResponse(restaurantId);
            return null;
        });

        restaurantRepository.delete(restaurant);
        loggingService.log(LogLevel.INFO, String.format("%s %s NAME=%s %s", loggingMessage, restaurantId, restaurant.getName(), Message.DELETE.getMessage()));
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
