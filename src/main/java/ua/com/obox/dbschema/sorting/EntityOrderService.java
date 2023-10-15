package ua.com.obox.dbschema.sorting;

import lombok.RequiredArgsConstructor;
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
    private static final ResourceBundle translation = ResourceBundle.getBundle("translation.messages");

    public void createEntityOrder(EntityOrder request, String acceptLanguage) {
        String finalAcceptLanguage = CheckHeader.checkHeaderLanguage(acceptLanguage);
        String entityUUID = request.getEntityId();
        Map<String, String> fieldErrors = new ResponseErrorMap<>();
        if (!String.join(",", request.getSortedArray()).matches("([0-9a-fA-F-]{36}(,)?)+")) {
            fieldErrors.put("sorted_list", translation.getString(finalAcceptLanguage + ".badSortedList"));
        }
        validationReference(request, fieldErrors, finalAcceptLanguage,
                entityOrderRepository, restaurantRepository, menuRepository, categoryRepository); // validate reference

        if (fieldErrors.size() > 0)
            throw new BadFieldsResponse(HttpStatus.BAD_REQUEST, fieldErrors);

        loggingService.log(LogLevel.INFO, String.format("Update/Create sorting list for %s UUID=%s", request.getReferenceType(), entityUUID));
    }

    private static void checkEntityInDatabase(Optional<?> foundEntity, EntityOrder request, String acceptLanguage, EntityOrderRepository entityOrderRepository, RestaurantRepository restaurantRepository, MenuRepository menuRepository, CategoryRepository categoryRepository) {
        if (foundEntity.isEmpty()){
            ExceptionTools.notFoundResponse(".entityOrderNotFound", acceptLanguage, request.getEntityId());
        }

        EntityOrder entityOrderExist = entityOrderRepository.findByEntityId(request.getEntityId()).orElse(null);
        EntityOrder entityOrderToSave;

        if (entityOrderExist != null) {
            entityOrderExist.setSortedArray(request.getSortedArray(), entityOrderRepository);
            entityOrderExist.setUpdatedAt(Instant.now().getEpochSecond());
            entityOrderToSave = entityOrderExist;
        } else {
            request.setSortedArray(request.getSortedArray(), entityOrderRepository);
            request.setCreatedAt(Instant.now().getEpochSecond());
            request.setUpdatedAt(Instant.now().getEpochSecond());
            entityOrderToSave = request;
        }
        entityOrderRepository.save(entityOrderToSave);
    }

    private static void validationReference(EntityOrder request, Map<String, String> fieldErrors, String finalAcceptLanguage,
                                            EntityOrderRepository entityOrderRepository, RestaurantRepository restaurantRepository, MenuRepository menuRepository, CategoryRepository categoryRepository) {
        switch (request.getReferenceType()) {
            case "DISH" -> {
                var categoryInfo = categoryRepository.findByCategoryId(request.getEntityId());
                checkEntityInDatabase(categoryInfo, request, finalAcceptLanguage, entityOrderRepository, restaurantRepository, menuRepository, categoryRepository);
            }
            case "CATEGORY" -> {
                var menuInfo = menuRepository.findByMenuId(request.getEntityId());
                checkEntityInDatabase(menuInfo, request, finalAcceptLanguage, entityOrderRepository, restaurantRepository, menuRepository, categoryRepository);
            }
            case "MENU" -> {
                var restaurantInfo = restaurantRepository.findByRestaurantId(request.getEntityId());
                checkEntityInDatabase(restaurantInfo, request, finalAcceptLanguage, entityOrderRepository, restaurantRepository, menuRepository, categoryRepository);
            }
            default ->
                    fieldErrors.put("reference_type", translation.getString(finalAcceptLanguage + ".badReferenceType"));
        }
    }
}
