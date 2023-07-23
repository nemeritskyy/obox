package ua.com.obox.dbschema.tenant;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import ua.com.obox.dbschema.restaurant.Restaurant;
import ua.com.obox.dbschema.restaurant.RestaurantRepository;
import ua.com.obox.dbschema.restaurant.RestaurantResponse;
import ua.com.obox.dbschema.tools.State;
import ua.com.obox.dbschema.tools.exception.Message;
import ua.com.obox.dbschema.tools.exception.ExceptionTools;
import ua.com.obox.dbschema.tools.logging.LogLevel;
import ua.com.obox.dbschema.tools.logging.LoggingService;

import java.util.*;

@Service
@RequiredArgsConstructor
public class TenantService {
    private final TenantRepository tenantRepository;
    private final RestaurantRepository restaurantRepository;
    private final LoggingService loggingService;
    private String loggingMessage;

    public List<RestaurantResponse> getAllRestaurantsByTenantId(String tenantId) {
        loggingMessage = ExceptionTools.generateLoggingMessage("getAllRestaurantsByTenantId", tenantId);
        List<Restaurant> restaurants = restaurantRepository.findAllByTenant_TenantId(tenantId);
        if (restaurants.isEmpty()) {
            loggingService.log(LogLevel.ERROR, loggingMessage + Message.NOT_FOUND.getMessage());
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Restaurants with Tenant id " + tenantId + Message.NOT_FOUND.getMessage(), null);
        }
        List<RestaurantResponse> responseList = new ArrayList<>();

        for (Restaurant restaurant : restaurants) {
            RestaurantResponse response = RestaurantResponse.builder()
                    .restaurantId(restaurant.getRestaurantId())
                    .address(restaurant.getAddress())
                    .name(restaurant.getName())
                    .tenantId(restaurant.getTenant().getTenantId())
                    .build();
            responseList.add(response);
        }

        loggingService.log(LogLevel.INFO, loggingMessage + Message.FIND_COUNT.getMessage() + responseList.size());
        return responseList;
    }

    public TenantResponse getTenantById(String tenantId) {
        loggingMessage = ExceptionTools.generateLoggingMessage("getTenantById", tenantId);
        var tenantInfo = tenantRepository.findByTenantId(tenantId);
        Tenant tenant = tenantInfo.orElseThrow(() -> {
            loggingService.log(LogLevel.ERROR, loggingMessage + Message.NOT_FOUND.getMessage());
            return new ResponseStatusException(HttpStatus.NOT_FOUND, "Tenant with id " + tenantId + Message.NOT_FOUND.getMessage());
        });
        if (tenant.getState().equals(State.DISABLE)) {
            loggingService.log(LogLevel.ERROR, loggingMessage + Message.FORBIDDEN.getMessage());
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Tenant with id " + tenantId + Message.FORBIDDEN.getMessage());
        }
        loggingService.log(LogLevel.INFO, loggingMessage);
        return TenantResponse.builder()
                .tenantId(tenant.getTenantId())
                .name(tenant.getName())
                .build();
    }

    public TenantResponseId createTenant(Tenant request) {
        loggingMessage = ExceptionTools.generateLoggingMessage("createTenant", request.getName());
        Tenant tenant = Tenant.builder()
                .name(request.getName().trim()) // delete whitespaces
                .state(State.ENABLE)
                .build();
        tenantRepository.save(tenant);
        loggingService.log(LogLevel.INFO, loggingMessage + " id=" + tenant.getTenantId() + Message.CREATE.getMessage());
        return TenantResponseId.builder()
                .tenantId(tenant.getTenantId())
                .build();
    }

    public void patchTenantById(String tenantId, Tenant request) {
        loggingMessage = ExceptionTools.generateLoggingMessage("patchTenantById", tenantId);
        var tenantInfo = tenantRepository.findByTenantId(tenantId);
        Tenant tenant = tenantInfo.orElseThrow(() -> {
            loggingService.log(LogLevel.ERROR, loggingMessage + Message.NOT_FOUND.getMessage());
            return new ResponseStatusException(HttpStatus.NOT_FOUND, "Tenant with id " + tenantId + Message.NOT_FOUND.getMessage());
        });
        String oldName = tenant.getName();
        tenant.setName(request.getName().trim()); // delete whitespaces
        tenantRepository.save(tenant);
        loggingService.log(LogLevel.INFO, loggingMessage + " OLD name=" + oldName + " NEW name=" + request.getName() + Message.UPDATE.getMessage());
    }

    public void deleteTenantById(String tenantId, boolean forceDelete) {
        loggingMessage = ExceptionTools.generateLoggingMessage("deleteTenantById", tenantId);
        var tenantInfo = tenantRepository.findByTenantId(tenantId);
        Tenant tenant = tenantInfo.orElseThrow(() -> {
            loggingService.log(LogLevel.ERROR, loggingMessage + Message.NOT_FOUND.getMessage());
            return new ResponseStatusException(HttpStatus.NOT_FOUND, "Tenant with id " + tenantId + Message.NOT_FOUND.getMessage());
        });
        if (!forceDelete) {
            tenant.setState(State.DISABLE);
            tenantRepository.save(tenant);
        } else {
            tenantRepository.delete(tenant);
        }
        loggingService.log(LogLevel.INFO, loggingMessage + " name=" + tenant.getName() + Message.DELETE.getMessage());
    }

}
