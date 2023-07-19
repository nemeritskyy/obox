package ua.com.obox.dbschema.tenant;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import ua.com.obox.dbschema.restaurant.Restaurant;
import ua.com.obox.dbschema.restaurant.RestaurantRepository;
import ua.com.obox.dbschema.restaurant.RestaurantResponse;
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
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, Message.NOT_FOUND.getMessage().trim(), null);
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
            return new ResponseStatusException(HttpStatus.NOT_FOUND, Message.NOT_FOUND.getMessage().trim());
        });
        loggingService.log(LogLevel.INFO, loggingMessage + Message.GET_BY_ID.getMessage());
        return TenantResponse.builder()
                .tenantId(tenant.getTenantId())
                .name(tenant.getName())
                .build();
    }

    public TenantResponseId createTenant(Tenant request) {
        loggingMessage = ExceptionTools.generateLoggingMessage("createTenant", request.toString());
        Tenant tenant = Tenant.builder()
                .name(request.getName().trim()) // delete whitespaces
                .build();
        tenantRepository.save(tenant);
        loggingService.log(LogLevel.INFO, loggingMessage + Message.CREATE.getMessage());
        return TenantResponseId.builder()
                .tenantId(tenant.getTenantId())
                .build();
    }

    public void patchTenantById(String tenantId, Tenant request) {
        loggingMessage = ExceptionTools.generateLoggingMessage("patchTenantById", tenantId);
        var tenantInfo = tenantRepository.findByTenantId(tenantId);
        Tenant tenant = tenantInfo.orElseThrow(() -> {
            loggingService.log(LogLevel.ERROR, loggingMessage + Message.NOT_FOUND.getMessage());
            return new ResponseStatusException(HttpStatus.NOT_FOUND, Message.NOT_FOUND.getMessage().trim());
        });
        String oldName = tenant.getName();
        tenant.setName(request.getName().trim()); // delete whitespaces
        tenantRepository.save(tenant);
        loggingService.log(LogLevel.INFO, loggingMessage + " OLD name=" + oldName + " NEW" + Message.UPDATE.getMessage());
    }

    public void deleteTenantById(String tenantId) {
        loggingMessage = ExceptionTools.generateLoggingMessage("deleteTenantById", tenantId);
        var tenantInfo = tenantRepository.findByTenantId(tenantId);
        Tenant tenant = tenantInfo.orElseThrow(() -> {
            loggingService.log(LogLevel.ERROR, loggingMessage + Message.NOT_FOUND.getMessage());
            return new ResponseStatusException(HttpStatus.NOT_FOUND, Message.NOT_FOUND.getMessage().trim());
        });
        tenantRepository.delete(tenant);
        loggingService.log(LogLevel.INFO, loggingMessage + Message.DELETE.getMessage());
    }

}
