package ua.com.obox.authserver.user;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import ua.com.obox.dbschema.category.Category;
import ua.com.obox.dbschema.category.CategoryRepository;
import ua.com.obox.dbschema.dish.Dish;
import ua.com.obox.dbschema.dish.DishRepository;
import ua.com.obox.dbschema.mark.Mark;
import ua.com.obox.dbschema.mark.MarkRepository;
import ua.com.obox.dbschema.menu.Menu;
import ua.com.obox.dbschema.menu.MenuRepository;
import ua.com.obox.dbschema.restaurant.Restaurant;
import ua.com.obox.dbschema.restaurant.RestaurantRepository;
import ua.com.obox.dbschema.tenant.Tenant;
import ua.com.obox.dbschema.tenant.TenantRepository;
import ua.com.obox.dbschema.tools.attachment.ReferenceType;
import ua.com.obox.dbschema.tools.exception.ExceptionTools;
import ua.com.obox.dbschema.tools.response.BadFieldsResponse;

import java.util.*;

@Service
@RequiredArgsConstructor
public class UserService {
    private final DishRepository dishRepository;
    private final CategoryRepository categoryRepository;
    private final MenuRepository menuRepository;
    private final RestaurantRepository restaurantRepository;
    private final TenantRepository tenantRepository;
    private final MarkRepository markRepository;

    private static final ResourceBundle translation = ResourceBundle.getBundle("translation.messages");
    private final Map<String, String> accessCache = new HashMap<>();

    public void checkPermissionForUser(ReferenceType referenceType, String requestToEntityId, String acceptLanguage) {
        System.out.println("join check permission");
        if (!this.haveSuperUserPermission()) {
            String tenantId = this.getTenantIdFromSecurityContextHolder();
            String tenantIdFromEntity = "";
            if (!accessCache.containsKey(requestToEntityId)) {
                switch (referenceType) {
                    case dish -> {
                        Optional<Dish> checkDishAccess = dishRepository.findByDishId(requestToEntityId);
                        tenantIdFromEntity = checkDishAccess
                                .map(dish -> dish.getCategory().getMenu().getRestaurant().getTenant().getTenantId())
                                .orElse(null);
                    }
                    case category -> {
                        Optional<Category> checkCategoryAccess = categoryRepository.findByCategoryId(requestToEntityId);
                        tenantIdFromEntity = checkCategoryAccess
                                .map(category -> category.getMenu().getRestaurant().getTenant().getTenantId())
                                .orElse(null);
                    }
                    case menu -> {
                        Optional<Menu> checkMenuAccess = menuRepository.findByMenuId(requestToEntityId);
                        tenantIdFromEntity = checkMenuAccess
                                .map(menu -> menu.getRestaurant().getTenant().getTenantId())
                                .orElse(null);
                    }
                    case restaurant -> {
                        Optional<Restaurant> checkRestaurantAccess = restaurantRepository.findByRestaurantId(requestToEntityId);
                        tenantIdFromEntity = checkRestaurantAccess
                                .map(restaurant -> restaurant.getTenant().getTenantId())
                                .orElse(null);
                    }
                    case tenant -> {
                        Optional<Tenant> checkTenantAccess = tenantRepository.findByTenantId(requestToEntityId);
                        tenantIdFromEntity = checkTenantAccess
                                .map(Tenant::getTenantId)
                                .orElse(null);
                    }
                    case mark -> {
                        Optional<Mark> checkMarkAccess = markRepository.findByMarkId(requestToEntityId);
                        tenantIdFromEntity = checkMarkAccess
                                .map(mark -> Objects.requireNonNull(restaurantRepository.findByRestaurantId(mark.getReferenceId()).orElse(null)).getTenant().getTenantId())
                                .orElse(null);
                    }
                    default -> throwNotFound(ReferenceType.entity, requestToEntityId, acceptLanguage);
                }
                System.out.println("tenant from entity " + tenantIdFromEntity);
                if (tenantId.equals(tenantIdFromEntity)) {
                    accessCache.put(requestToEntityId, tenantId);
                    System.out.println("have access");
                } else {
                    throwNotFound(referenceType, requestToEntityId, acceptLanguage);
                }
            } else {
                System.out.println("exist in cache");
                if (!accessCache.get(requestToEntityId).equals(tenantId))
                    throwNotFound(referenceType, requestToEntityId, acceptLanguage);
            }
        }
    }

    public void checkPermissionForUser(ReferenceType referenceType, String requestToEntityId, String referenceInputType, String acceptLanguage) {
        if (referenceInputType.equals(referenceType.toString())) {
            checkPermissionForUser(referenceType, requestToEntityId, acceptLanguage);
        } else {
            Map<String, String> errors = new HashMap<>();
            errors.put("reference_type", translation.getString(acceptLanguage + ".badReferenceType"));
            throw new BadFieldsResponse(HttpStatus.BAD_REQUEST, errors);
        }
    }

    private String getTenantIdFromSecurityContextHolder() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();
        return user.getTenant().getTenantId();
    }

    private boolean haveSuperUserPermission() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication.getPrincipal() instanceof User) {
            User user = (User) authentication.getPrincipal();
            GrantedAuthority superUserAuthority = new SimpleGrantedAuthority("ROLE_ADMIN");
            return user.getAuthorities().contains(superUserAuthority);
        }
        return false;
    }

    private void throwNotFound(ReferenceType referenceType, String requestToEntityId, String acceptLanguage) {
        ExceptionTools.notFoundExceptionWithoutLogging("." + referenceType.name() + "NotFound", acceptLanguage, requestToEntityId);
    }
}
