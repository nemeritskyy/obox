package ua.com.obox.dbschema.category;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import ua.com.obox.dbschema.menu.Menu;
import ua.com.obox.dbschema.menu.MenuRepository;
import ua.com.obox.dbschema.dish.Dish;
import ua.com.obox.dbschema.dish.DishRepository;
import ua.com.obox.dbschema.dish.DishResponse;
import ua.com.obox.dbschema.sorting.EntityOrder;
import ua.com.obox.dbschema.sorting.EntityOrderRepository;
import ua.com.obox.dbschema.tools.RequiredServiceHelper;
import ua.com.obox.dbschema.tools.Validator;
import ua.com.obox.dbschema.tools.exception.ExceptionTools;
import ua.com.obox.dbschema.tools.exception.Message;
import ua.com.obox.dbschema.tools.logging.LogLevel;
import ua.com.obox.dbschema.tools.logging.LoggingService;
import ua.com.obox.dbschema.tools.response.BadFieldsResponse;
import ua.com.obox.dbschema.tools.response.ResponseErrorMap;
import ua.com.obox.dbschema.tools.services.UpdateServiceHelper;
import ua.com.obox.dbschema.tools.translation.CheckHeader;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {
    private final CategoryRepository categoryRepository;
    private final MenuRepository menuRepository;
    private final DishRepository dishRepository;
    private final EntityOrderRepository entityOrderRepository;
    private final LoggingService loggingService;
    private final UpdateServiceHelper serviceHelper;
    private final RequiredServiceHelper requiredServiceHelper;
    private final ResourceBundle translation = ResourceBundle.getBundle("translation.messages");

    public List<DishResponse> getAllDishesByCategoryId(String categoryId, String acceptLanguage) {
        String finalAcceptLanguage = CheckHeader.checkHeaderLanguage(acceptLanguage);

        var categoryInfo = categoryRepository.findByCategoryId(categoryId);

        categoryInfo.orElseThrow(() -> {
            ExceptionTools.notFoundResponse(".categoryNotFound", finalAcceptLanguage, categoryId);
            return null;
        });

        List<Dish> dishes = dishRepository.findAllByCategory_CategoryIdOrderByName(categoryId);

        // for sorting results
        EntityOrder sortingExist = entityOrderRepository.findByEntityId(categoryId).orElse(null);
        if (sortingExist != null) {
            List<String> dishIdsInOrder = Arrays.stream(sortingExist.getSortedList().split(",")).toList();
            dishes.sort(Comparator.comparingInt(dish -> {
                int index = dishIdsInOrder.indexOf(dish.getDishId());
                return index != -1 ? index : Integer.MAX_VALUE;
            }));
        }

        List<DishResponse> responseList = dishes.stream()
                .map(dish -> {
                    List<String> allergensList = (dish.getAllergens() != null) ? Arrays.asList(dish.getAllergens().split("::")) : new ArrayList<>();
                    List<String> tagsList = (dish.getTags() != null) ? Arrays.asList(dish.getTags().split("::")) : new ArrayList<>();

                    Collections.sort(allergensList);
                    Collections.sort(tagsList);

                    return DishResponse.builder()
                            .dishId(dish.getDishId())
                            .categoryId(dish.getCategory().getCategoryId())
                            .name(dish.getName())
                            .description(dish.getDescription())
                            .cookingTime(dish.getCooking_time())
                            .price(dish.getPrice())
                            .specialPrice(dish.getSpecialPrice())
                            .weight(dish.getWeight())
                            .weightUnit(dish.getWeight_unit())
                            .calories(dish.getCalories())
                            .inStock(dish.getIn_stock())
                            .state(dish.getState())
                            .allergens(allergensList)
                            .tags(tagsList)
                            .associatedId(dish.getAssociatedId())
                            .image(dish.getImage())
                            .build();
                })
                .collect(Collectors.toList());

        loggingService.log(LogLevel.INFO, String.format("getAllDishesByCategoryId %s %s %d", categoryId, Message.FIND_COUNT.getMessage(), responseList.size()));
        return responseList;
    }

    public CategoryResponse getCategoryById(String categoryId, String acceptLanguage) {
        String finalAcceptLanguage = CheckHeader.checkHeaderLanguage(acceptLanguage);

        var categoryInfo = categoryRepository.findByCategoryId(categoryId);

        Category category = categoryInfo.orElseThrow(() -> {
            ExceptionTools.notFoundResponse(".categoryNotFound", finalAcceptLanguage, categoryId);
            return null;
        });

        loggingService.log(LogLevel.INFO, String.format("getCategoryById %s", categoryId));
        return CategoryResponse.builder()
                .categoryId(category.getCategoryId())
                .menuId(category.getMenu().getMenuId())
                .name(category.getName())
                .description(category.getDescription())
                .state(category.getState())
                .build();
    }

    public CategoryResponseId createCategory(Category request, String acceptLanguage) {
        String finalAcceptLanguage = CheckHeader.checkHeaderLanguage(acceptLanguage);
        Map<String, String> fieldErrors = new ResponseErrorMap<>();

        request.setMenuIdForCategory(request.getMenu_id());

        Menu menu = menuRepository.findByMenuId(request.getMenu().getMenuId())
                .orElseGet(() -> {
                    fieldErrors.put("menu_id", String.format(translation.getString(finalAcceptLanguage + ".menuNotFound"), request.getMenu().getMenuId()));
                    return null;
                });

        Category category = Category.builder()
                .menu(menu)
                .build();

        if (request.getName() != null && !categoryRepository.findAllByMenu_MenuIdAndName(request.getMenu_id(), Validator.removeExtraSpaces(request.getName())).isEmpty()) {
            fieldErrors.put("name", translation.getString(finalAcceptLanguage + ".categoryExists"));
        } else {
            fieldErrors.put("name", serviceHelper.updateNameField(category::setName, request.getName(), finalAcceptLanguage));
        }

        fieldErrors.put("description", serviceHelper.updateVarcharField(category::setDescription, request.getDescription(), "description", finalAcceptLanguage));

        fieldErrors.put("state", serviceHelper.updateState(category::setState, request.getState(), finalAcceptLanguage));

        if (fieldErrors.size() > 0)
            throw new BadFieldsResponse(HttpStatus.BAD_REQUEST, fieldErrors);

        category.setCreatedAt(Instant.now().getEpochSecond());
        category.setUpdatedAt(Instant.now().getEpochSecond());
        categoryRepository.save(category);

        loggingService.log(LogLevel.INFO, String.format("createCategory %s UUID=%s %s", request.getName(), category.getCategoryId(), Message.CREATE.getMessage()));
        return CategoryResponseId.builder()
                .categoryId(category.getCategoryId())
                .build();
    }

    public void patchCategoryById(String categoryId, Category request, String acceptLanguage) {
        String finalAcceptLanguage = CheckHeader.checkHeaderLanguage(acceptLanguage);
        Map<String, String> fieldErrors = new ResponseErrorMap<>();

        var categoryInfo = categoryRepository.findByCategoryId(categoryId);

        Category category = categoryInfo.orElseThrow(() -> {
            ExceptionTools.notFoundResponse(".categoryNotFound", finalAcceptLanguage, categoryId);
            return null;
        });

        if (request.getName() != null && !category.getName().equals(Validator.removeExtraSpaces(request.getName()))) {
            if (!categoryRepository.findAllByMenu_MenuIdAndName(category.getMenu().getMenuId(), Validator.removeExtraSpaces(request.getName())).isEmpty()) {
                fieldErrors.put("name", translation.getString(finalAcceptLanguage + ".categoryExists"));
            } else {
                fieldErrors.put("name", requiredServiceHelper.updateNameIfNeeded(request.getName(), category, finalAcceptLanguage));
            }
        }

        if (request.getDescription() != null)
            fieldErrors.put("description", serviceHelper.updateVarcharField(category::setDescription, request.getDescription(), "description", finalAcceptLanguage));

        if (request.getState() != null)
            fieldErrors.put("state", serviceHelper.updateState(category::setState, request.getState(), finalAcceptLanguage));

        if (fieldErrors.size() > 0)
            throw new BadFieldsResponse(HttpStatus.BAD_REQUEST, fieldErrors);

        category.setUpdatedAt(Instant.now().getEpochSecond());
        categoryRepository.save(category);
        loggingService.log(LogLevel.INFO, String.format("patchCategoryById %s %s", categoryId, Message.UPDATE.getMessage()));
    }

    public void deleteCategoryById(String categoryId, String acceptLanguage) {
        String finalAcceptLanguage = CheckHeader.checkHeaderLanguage(acceptLanguage);

        var categoryInfo = categoryRepository.findByCategoryId(categoryId);

        Category category = categoryInfo.orElseThrow(() -> {
            ExceptionTools.notFoundResponse(".categoryNotFound", finalAcceptLanguage, categoryId);
            return null;
        });

        categoryRepository.delete(category);
        loggingService.log(LogLevel.INFO, String.format("deleteCategoryById %s NAME=%s %s", categoryId, category.getName(), Message.DELETE.getMessage()));
    }
}