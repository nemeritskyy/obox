package ua.com.obox.dbschema.category;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import ua.com.obox.dbschema.menu.Menu;
import ua.com.obox.dbschema.menu.MenuRepository;
import ua.com.obox.dbschema.dish.Dish;
import ua.com.obox.dbschema.dish.DishRepository;
import ua.com.obox.dbschema.dish.DishResponse;
import ua.com.obox.dbschema.tools.RequiredServiceHelper;
import ua.com.obox.dbschema.tools.exception.Message;
import ua.com.obox.dbschema.tools.logging.LogLevel;
import ua.com.obox.dbschema.tools.logging.LoggingService;
import ua.com.obox.dbschema.tools.response.BadFieldsResponse;
import ua.com.obox.dbschema.tools.response.ResponseErrorMap;
import ua.com.obox.dbschema.tools.services.AbstractResponseService;
import ua.com.obox.dbschema.tools.services.LoggingResponseHelper;
import ua.com.obox.dbschema.tools.services.UpdateServiceHelper;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService extends AbstractResponseService {
    private final CategoryRepository categoryRepository;
    private final MenuRepository menuRepository;
    private final DishRepository dishRepository;
    private final LoggingService loggingService;
    private final UpdateServiceHelper serviceHelper;
    private final RequiredServiceHelper requiredServiceHelper;
    private String loggingMessage;
    private String responseMessage;

    public List<DishResponse> getAllDishesByCategoryId(String categoryId) {
        loggingMessage = "getAllDishesByCategoryId";
        responseMessage = String.format("Dishes with Category id %s", categoryId);

        List<Dish> dishes = dishRepository.findAllByCategory_CategoryId(categoryId);
        if (dishes.isEmpty()) {
            notFoundResponse(categoryId);
        }

        List<DishResponse> responseList = dishes.stream()
                .map(dish -> DishResponse.builder()
                        .dishId(dish.getDishId())
                        .categoryId(dish.getCategory().getCategoryId())
                        .associatedId(dish.getAssociatedId())
                        .name(dish.getName())
                        .description(dish.getDescription())
                        .price(dish.getPrice())
                        .weight(dish.getWeight())
                        .calories(dish.getCalories())
                        .allergens(dish.getAllergens())
                        .tags(dish.getTags())
                        .imageUrl(dish.getImageUrl())
                        .state(dish.getState())
                        .build()).collect(Collectors.toList());

        loggingService.log(LogLevel.INFO, String.format("%s %s %s %d", loggingMessage, categoryId, Message.FIND_COUNT.getMessage(), responseList.size()));
        return responseList;
    }

    public CategoryResponse getCategoryById(String categoryId) {
        Category category;
        loggingMessage = "getCategoryById";
        responseMessage = String.format("Category with id %s", categoryId);
        var categoryInfo = categoryRepository.findByCategoryId(categoryId);

        category = categoryInfo.orElseThrow(() -> {
            notFoundResponse(categoryId);
            return null;
        });

        loggingService.log(LogLevel.INFO, String.format("%s %s", loggingMessage, categoryId));
        return CategoryResponse.builder()
                .categoryId(category.getCategoryId())
                .name(category.getName())
                .menuId(category.getMenu().getMenuId())
                .build();
    }

    public CategoryResponseId createCategory(Category request) {
        Menu menu;
        Category category;
        Map<String, String> fieldErrors = new ResponseErrorMap<>();
        loggingMessage = "createCategory";
        responseMessage = String.format("Menu with id %s", request.getMenu_id());

        request.setMenuIdForCategory(request.getMenu_id());

        menu = menuRepository.findByMenuId(request.getMenu().getMenuId())
                .orElseGet(() -> {
                    fieldErrors.put("menu_id", responseMessage + Message.NOT_FOUND.getMessage());
                    return null;
                });

        category = Category.builder()
                .menu(menu)
                .build();

        fieldErrors.put("name", serviceHelper.updateNameField(category::setName, request.getName(), "Name", loggingMessage));

        if (fieldErrors.size() > 0)
            throw new BadFieldsResponse(HttpStatus.BAD_REQUEST, fieldErrors);

        categoryRepository.save(category);

        loggingService.log(LogLevel.INFO, String.format("%s %s UUID=%s %s", loggingMessage, request.getName(), category.getCategoryId(), Message.CREATE.getMessage()));
        return CategoryResponseId.builder()
                .categoryId(category.getCategoryId())
                .build();
    }

    public void patchCategoryById(String categoryId, Category request) {
        Category category;
        Map<String, String> fieldErrors = new ResponseErrorMap<>();

        loggingMessage = "patchCategoryById";
        responseMessage = String.format("Category with id %s", categoryId);
        var categoryInfo = categoryRepository.findByCategoryId(categoryId);

        category = categoryInfo.orElseThrow(() -> {
            notFoundResponse(categoryId);
            return null;
        });

        if (request.getName() != null)
            fieldErrors.put("name", requiredServiceHelper.updateNameIfNeeded(request.getName(), category, loggingMessage));

        if (fieldErrors.size() > 0)
            throw new BadFieldsResponse(HttpStatus.BAD_REQUEST, fieldErrors);

        categoryRepository.save(category);
        loggingService.log(LogLevel.INFO, String.format("%s %s %s", loggingMessage, categoryId, Message.UPDATE.getMessage()));
    }

    public void deleteCategoryById(String categoryId) {
        Category category;
        loggingMessage = "deleteCategoryById";
        responseMessage = String.format("Category with id %s", categoryId);
        var categoryInfo = categoryRepository.findByCategoryId(categoryId);

        category = categoryInfo.orElseThrow(() -> {
            notFoundResponse(categoryId);
            return null;
        });

        categoryRepository.delete(category);
        loggingService.log(LogLevel.INFO, String.format("%s %s NAME=%s %s", loggingMessage, categoryId, category.getName(), Message.DELETE.getMessage()));
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
