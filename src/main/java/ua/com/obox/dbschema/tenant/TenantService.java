package ua.com.obox.dbschema.tenant;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import ua.com.obox.dbschema.tools.RequiredServiceHelper;
import ua.com.obox.dbschema.restaurant.Restaurant;
import ua.com.obox.dbschema.restaurant.RestaurantRepository;
import ua.com.obox.dbschema.restaurant.RestaurantResponse;
import ua.com.obox.dbschema.tools.response.ResponseErrorMap;
import ua.com.obox.dbschema.tools.response.BadFieldsResponse;
import ua.com.obox.dbschema.tools.services.UpdateServiceHelper;
import ua.com.obox.dbschema.tools.State;
import ua.com.obox.dbschema.tools.exception.Message;
import ua.com.obox.dbschema.tools.logging.LogLevel;
import ua.com.obox.dbschema.tools.services.AbstractResponseService;
import ua.com.obox.dbschema.tools.services.LoggingResponseHelper;
import ua.com.obox.dbschema.tools.logging.LoggingService;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TenantService extends AbstractResponseService {
    private final TenantRepository tenantRepository;
    private final RestaurantRepository restaurantRepository;
    private final LoggingService loggingService;
    private final UpdateServiceHelper serviceHelper;
    private final RequiredServiceHelper requiredServiceHelper;
    private String loggingMessage;
    private String responseMessage;

    public List<RestaurantResponse> getAllRestaurantsByTenantId(String tenantId) {
        loggingMessage = "getAllRestaurantsByTenantId";
        responseMessage = String.format("Restaurants with Tenant id %s", tenantId);

        List<Restaurant> restaurants = restaurantRepository.findAllByTenant_TenantId(tenantId);
        if (restaurants.isEmpty()) {
            notFoundResponse(tenantId);
        }

        List<RestaurantResponse> responseList = restaurants.stream()
                .map(restaurant -> RestaurantResponse.builder()
                        .restaurantId(restaurant.getRestaurantId())
                        .address(restaurant.getAddress())
                        .name(restaurant.getName())
                        .tenantId(restaurant.getTenant().getTenantId())
                        .build())
                .collect(Collectors.toList());

        loggingService.log(LogLevel.INFO, String.format("%s %s %s %d", loggingMessage, tenantId, Message.FIND_COUNT.getMessage(), responseList.size()));
        return responseList;
    }

    public TenantResponse getTenantById(String tenantId) {
        Tenant tenant;
        loggingMessage = "getTenantById";
        responseMessage = String.format("Tenant with id %s", tenantId);
        var tenantInfo = tenantRepository.findByTenantId(tenantId);

        tenant = tenantInfo.orElseThrow(() -> {
            notFoundResponse(tenantId);
            return null;
        });

        if (tenant.getState().equals(State.DISABLED)) {
            forbiddenResponse(tenantId);
        }

        loggingService.log(LogLevel.INFO, String.format("%s %s", loggingMessage, tenantId));
        return TenantResponse.builder()
                .tenantId(tenant.getTenantId())
                .name(tenant.getName())
                .build();
    }


    public TenantResponseId createTenant(Tenant request) {
        Tenant tenant;
        Map<String, String> fieldErrors = new ResponseErrorMap<>();
        loggingMessage = "createTenant";

        tenant = Tenant.builder()
                .state(State.ENABLED)
                .build();

        fieldErrors.put("name", serviceHelper.updateNameField(tenant::setName, request.getName(), "Name", loggingMessage));

        if (fieldErrors.size() > 0)
            throw new BadFieldsResponse(HttpStatus.BAD_REQUEST, fieldErrors);

        tenantRepository.save(tenant);

        loggingService.log(LogLevel.INFO, String.format("%s %s UUID=%s %s", loggingMessage, request.getName(), tenant.getTenantId(), Message.CREATE.getMessage()));
        return TenantResponseId.builder()
                .tenantId(tenant.getTenantId())
                .build();
    }

    public void patchTenantById(String tenantId, Tenant request) {
        Tenant tenant;
        Map<String, String> fieldErrors = new ResponseErrorMap<>();
        loggingMessage = "patchTenantById";
        responseMessage = String.format("Tenant with id %s", tenantId);
        var tenantInfo = tenantRepository.findByTenantId(tenantId);

        tenant = tenantInfo.orElseThrow(() -> {
            notFoundResponse(tenantId);
            return null;
        });

        fieldErrors.put("name", requiredServiceHelper.updateNameIfNeeded(request.getName(), tenant, loggingMessage));

        if (fieldErrors.size() > 0)
            throw new BadFieldsResponse(HttpStatus.BAD_REQUEST, fieldErrors);

        tenantRepository.save(tenant);
        loggingService.log(LogLevel.INFO, String.format("%s %s %s", loggingMessage, tenantId, Message.UPDATE.getMessage()));
    }

    public void deleteTenantById(String tenantId, boolean forceDelete) {
        Tenant tenant;
        loggingMessage = "deleteTenantById";
        var tenantInfo = tenantRepository.findByTenantId(tenantId);

        tenant = tenantInfo.orElseThrow(() -> {
            notFoundResponse(tenantId);
            return null;
        });

        if (!forceDelete) {
            tenant.setState(State.DISABLED);
            tenantRepository.save(tenant);
        } else {
            tenantRepository.delete(tenant);
        }

        loggingService.log(LogLevel.INFO, String.format("%s %s NAME=%s %s", loggingMessage, tenantId, tenant.getName(), Message.DELETE.getMessage()));
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
    public void forbiddenResponse(String entityId) {
        LoggingResponseHelper.loggingThrowException(
                entityId,
                LogLevel.ERROR, HttpStatus.FORBIDDEN,
                loggingMessage, responseMessage + Message.FORBIDDEN.getMessage(),
                loggingService);
    }
}
