package ua.com.obox.dbschema.sorting;

import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import ua.com.obox.dbschema.category.CategoryRepository;
import ua.com.obox.dbschema.menu.MenuRepository;
import ua.com.obox.dbschema.restaurant.RestaurantRepository;
import ua.com.obox.dbschema.tools.exception.ExceptionTools;
import ua.com.obox.dbschema.tools.logging.LogLevel;
import ua.com.obox.dbschema.tools.logging.LoggingService;
import ua.com.obox.dbschema.tools.response.BadFieldsResponse;
import ua.com.obox.dbschema.tools.response.ResponseErrorMap;
import ua.com.obox.dbschema.tools.translation.CheckHeader;

import java.time.Instant;
import java.util.*;

@Service
@RequiredArgsConstructor
public class EntityOrderService {
    private final EntityOrderRepository entityOrderRepository;
    private final MenuRepository menuRepository;
    private final RestaurantRepository restaurantRepository;
    private final CategoryRepository categoryRepository;
    private final LoggingService loggingService;
    private final ResourceBundle translation = ResourceBundle.getBundle("translation.messages");

    public void createEntityOrder(EntityOrder request, String acceptLanguage) {
        String finalAcceptLanguage = CheckHeader.checkHeaderLanguage(acceptLanguage);
        String entityUUID = request.getEntityId();
        Map<String, String> fieldErrors = new ResponseErrorMap<>();
        EntityOrder entityOrderExist = entityOrderRepository.findByEntityId(entityUUID).orElseGet(() -> {
            String[] validValues = {"MENU", "CATEGORY", "DISH"};
            Map<String, JpaRepository<?, ?>> entityList = new HashMap<>(); // reference for parent entity
            entityList.put("MENU", restaurantRepository);
            entityList.put("CATEGORY", menuRepository);
            entityList.put("DISH", categoryRepository);
            if (Arrays.asList(validValues).contains(request.getReferenceType())) {
                JpaRepository<?, ?> repository = entityList.get(request.getReferenceType());
                if (repository instanceof RestaurantRepository restaurantRepo) {
                    var restaurantInfo = restaurantRepo.findByRestaurantId(entityUUID);
                    try {
                        checkEntityInDatabase(restaurantInfo, request, finalAcceptLanguage);
                    } catch (Throwable e) {
                        throw new RuntimeException(e);
                    }
                } else if (repository instanceof MenuRepository menuRepo) {
                    var menuInfo = menuRepo.findByMenuId(entityUUID);
                    try {
                        checkEntityInDatabase(menuInfo, request, finalAcceptLanguage);
                    } catch (Throwable e) {
                        throw new RuntimeException(e);
                    }
                } else if (repository instanceof CategoryRepository categoryRepo) {
                    var categoryInfo = categoryRepo.findByCategoryId(entityUUID);
                    try {
                        checkEntityInDatabase(categoryInfo, request, finalAcceptLanguage);
                    } catch (Throwable e) {
                        throw new RuntimeException(e);
                    }
                }
            } else {
                fieldErrors.put("reference_type", translation.getString(finalAcceptLanguage + ".badReferenceType"));
            }
            return null;
        });

        if (fieldErrors.size() > 0)
            throw new BadFieldsResponse(HttpStatus.BAD_REQUEST, fieldErrors);

        //update
        if (entityOrderExist != null) {
            entityOrderExist.setSortedArray(request.getSortedArray(), entityOrderRepository);
            entityOrderExist.setUpdatedAt(Instant.now().getEpochSecond());
            entityOrderRepository.save(entityOrderExist);
        }

        loggingService.log(LogLevel.INFO, String.format("Update/Create sorting list for %s UUID=%s", request.getReferenceType(), entityUUID));
    }

    private void checkEntityInDatabase(Optional<?> foundEntity, EntityOrder request, String acceptLanguage) {
        foundEntity.orElseGet(() -> {
            ExceptionTools.notFoundResponse(".entityOrderNotFound", acceptLanguage, request.getEntityId());
            return null;
        });

        request.setSortedArray(request.getSortedArray(), entityOrderRepository);
        request.setCreatedAt(Instant.now().getEpochSecond());
        request.setUpdatedAt(Instant.now().getEpochSecond());
        entityOrderRepository.save(request);
    }

}
