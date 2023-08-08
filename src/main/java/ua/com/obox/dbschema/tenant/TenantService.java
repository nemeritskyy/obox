package ua.com.obox.dbschema.tenant;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import ua.com.obox.dbschema.restaurant.Restaurant;
import ua.com.obox.dbschema.restaurant.RestaurantRepository;
import ua.com.obox.dbschema.restaurant.RestaurantResponse;
import ua.com.obox.dbschema.tools.State;
import ua.com.obox.dbschema.tools.exception.Message;
import ua.com.obox.dbschema.tools.logging.LogLevel;
import ua.com.obox.dbschema.tools.logging.LoggingResponseHelper;
import ua.com.obox.dbschema.tools.logging.LoggingService;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TenantService {
    private final TenantRepository tenantRepository;
    private final RestaurantRepository restaurantRepository;
    private final LoggingService loggingService;
    private String loggingMessage;
    private String responseMessage;

    public List<RestaurantResponse> getAllRestaurantsByTenantId(String tenantId) {
        loggingMessage = "getAllRestaurantsByTenantId";
        responseMessage = String.format("Restaurants with Tenant id %s", tenantId);

        List<Restaurant> restaurants = restaurantRepository.findAllByTenant_TenantId(tenantId);
        if (restaurants.isEmpty()) {
            NotFoundResponse(tenantId);
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
            NotFoundResponse(tenantId);
            return null;
        });

        if (tenant.getState().equals(State.DISABLED)) {
            ForbiddenResponse(tenantId);
        }

        loggingService.log(LogLevel.INFO, loggingMessage);
        return TenantResponse.builder()
                .tenantId(tenant.getTenantId())
                .name(tenant.getName())
                .build();
    }


    public TenantResponseId createTenant(Tenant request) {
        Tenant tenant;
        loggingMessage = "createTenant";

        tenant = Tenant.builder()
                .name(request.getName().trim())
                .state(State.ENABLED)
                .build();
        tenantRepository.save(tenant);

        loggingService.log(LogLevel.INFO, String.format("%s %s UUID=%s %s", loggingMessage, request.getName(), tenant.getTenantId(), Message.CREATE.getMessage()));
        return TenantResponseId.builder()
                .tenantId(tenant.getTenantId())
                .build();
    }

    public void patchTenantById(String tenantId, Tenant request) {
        Tenant tenant;
        loggingMessage = "patchTenantById";
        responseMessage = String.format("Tenant with id %s", tenantId);
        var tenantInfo = tenantRepository.findByTenantId(tenantId);

        tenant = tenantInfo.orElseThrow(() -> {
            NotFoundResponse(tenantId);
            return null;
        });

        loggingService.log(LogLevel.INFO, String.format("%s %s OLD NAME=%s NEW NAME=%s %s", loggingMessage, tenant.getTenantId(), tenant.getName(), request.getName().trim(), Message.UPDATE.getMessage()));
        tenant.setName(request.getName().trim());
        tenantRepository.save(tenant);
    }

    public void deleteTenantById(String tenantId, boolean forceDelete) {
        Tenant tenant;
        loggingMessage = "deleteTenantById";
        var tenantInfo = tenantRepository.findByTenantId(tenantId);

        tenant = tenantInfo.orElseThrow(() -> {
            NotFoundResponse(tenantId);
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

    private void NotFoundResponse(String tenantId) {
        LoggingResponseHelper.loggingThrowException(
                tenantId,
                LogLevel.ERROR, HttpStatus.NOT_FOUND,
                loggingMessage, responseMessage + Message.NOT_FOUND.getMessage(),
                loggingService);
    }

    private void ForbiddenResponse(String tenantId) {
        LoggingResponseHelper.loggingThrowException(
                tenantId,
                LogLevel.ERROR, HttpStatus.FORBIDDEN,
                loggingMessage, responseMessage + Message.FORBIDDEN.getMessage(),
                loggingService);
    }
}
