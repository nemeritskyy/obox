package ua.com.obox.authserver.user;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import ua.com.obox.dbschema.dish.Dish;
import ua.com.obox.dbschema.dish.DishRepository;
import ua.com.obox.dbschema.tools.attachment.ReferenceType;
import ua.com.obox.dbschema.tools.response.BadFieldsResponse;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final DishRepository dishRepository;
    private final Map<String, String> accessCache = new HashMap<>();

    public void checkPermissionForUser(ReferenceType referenceType, String requestToEntityId) {
        String tenantId = this.getTenantIdFromSecurityContextHolder();
        String tenantIdFromEntity;
        if (!accessCache.containsKey(requestToEntityId)) {
            switch (referenceType) {
                case dish -> {
                    Optional<Dish> checkDishAccess = dishRepository.findByDishId(requestToEntityId);
                    tenantIdFromEntity = checkDishAccess
                            .map(dish -> dish.getCategory().getMenu().getRestaurant().getTenant().getTenantId())
                            .orElse(null);
                }
                default -> throw new BadFieldsResponse(HttpStatus.FORBIDDEN);
            }
            if (tenantId.equals(tenantIdFromEntity)) {
                accessCache.put(requestToEntityId, tenantId);
                System.out.println("have access");
            } else {
                throw new BadFieldsResponse(HttpStatus.FORBIDDEN);
            }
        } else {
            System.out.println("exist in cache");
            if (!accessCache.get(requestToEntityId).equals(tenantId))
                throw new BadFieldsResponse(HttpStatus.FORBIDDEN);
        }
    }

    private String getTenantIdFromSecurityContextHolder() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();
        return user.getTenant().getTenantId();
    }
}
