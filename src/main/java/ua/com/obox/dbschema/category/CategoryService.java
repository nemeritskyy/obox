package ua.com.obox.dbschema.category;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import ua.com.obox.dbschema.menu.Menu;
import ua.com.obox.dbschema.menu.MenuRepository;
import ua.com.obox.dbschema.dish.Dish;
import ua.com.obox.dbschema.dish.DishRepository;
import ua.com.obox.dbschema.dish.DishResponse;
import ua.com.obox.dbschema.tools.exception.ExceptionTools;
import ua.com.obox.dbschema.tools.exception.Message;
import ua.com.obox.dbschema.tools.logging.LogLevel;
import ua.com.obox.dbschema.tools.logging.LoggingService;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {
    private final CategoryRepository categoryRepository;
    private final MenuRepository menuRepository;
    private final DishRepository dishRepository;
    private final LoggingService loggingService;
    private String loggingMessage;

    public List<DishResponse> getAllDishesByCategoryId(String categoryId) {
        loggingMessage = ExceptionTools.generateLoggingMessage("getAllDishesByCategoryId", categoryId);
        List<Dish> dishes = dishRepository.findAllByCategory_CategoryId(categoryId);
        if (dishes.isEmpty()) {
            loggingService.log(LogLevel.ERROR, loggingMessage + Message.NOT_FOUND.getMessage());
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Dishes with Category id " + categoryId + Message.NOT_FOUND.getMessage(), null);
        }
        List<DishResponse> responseList = new ArrayList<>();

        for (Dish dish : dishes) {
            DishResponse response = DishResponse.builder()
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
                    .build();
            responseList.add(response);
        }

        loggingService.log(LogLevel.INFO, loggingMessage + Message.FIND_COUNT.getMessage() + responseList.size());
        return responseList;
    }

    public CategoryResponse getCategoryById(String categoryId) {
        loggingMessage = ExceptionTools.generateLoggingMessage("getCategoryById", categoryId);
        var categoryInfo = categoryRepository.findByCategoryId(categoryId);
        Category category = categoryInfo.orElseThrow(() -> {
            loggingService.log(LogLevel.ERROR, loggingMessage + Message.NOT_FOUND.getMessage());
            return new ResponseStatusException(HttpStatus.NOT_FOUND, "Category with id " + categoryId + Message.NOT_FOUND.getMessage());
        });
        loggingService.log(LogLevel.INFO, loggingMessage);
        return CategoryResponse.builder()
                .categoryId(category.getCategoryId())
                .name(category.getName())
                .menuId(category.getMenu().getMenuId())
                .build();
    }

    public CategoryResponseId createCategory(Category request) {
        loggingMessage = ExceptionTools.generateLoggingMessage("createCategory", request.getMenu_id());
        request.setMenuIdForCategory(request.getMenu_id());
        Menu menu = menuRepository.findByMenuId(request.getMenu().getMenuId()).orElseThrow(() -> {
            loggingService.log(LogLevel.ERROR, loggingMessage + Message.MENU_NOT_FOUND.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Menu with id " + request.getMenu().getMenuId() + Message.NOT_FOUND.getMessage(), null);
        });
        Category category = Category.builder()
                .name(request.getName().trim()) // delete whitespaces
                .menu(menu)
                .build();
        categoryRepository.save(category);
        loggingService.log(LogLevel.INFO, loggingMessage + " id=" + category.getCategoryId() + Message.CREATE.getMessage());
        return CategoryResponseId.builder()
                .categoryId(category.getCategoryId())
                .build();
    }

    public void patchCategoryById(String categoryId, Category request) {
        loggingMessage = ExceptionTools.generateLoggingMessage("patchCategoryById", categoryId);
        var categoryInfo = categoryRepository.findByCategoryId(categoryId);
        Category category = categoryInfo.orElseThrow(() -> {
            loggingService.log(LogLevel.ERROR, loggingMessage + Message.NOT_FOUND.getMessage());
            return new ResponseStatusException(HttpStatus.NOT_FOUND, "Category with id " + categoryId + Message.NOT_FOUND.getMessage());
        });
        String oldName = category.getName();
        category.setName(request.getName().trim()); // delete whitespaces
        categoryRepository.save(category);
        loggingService.log(LogLevel.INFO, loggingMessage + " OLD name=" + oldName + " NEW name=" + request.getName() + Message.UPDATE.getMessage());
    }

    public void deleteCategoryById(String categoryId) {
        loggingMessage = ExceptionTools.generateLoggingMessage("deleteCategoryById", categoryId);
        var categoryInfo = categoryRepository.findByCategoryId(categoryId);
        Category category = categoryInfo.orElseThrow(() -> {
            loggingService.log(LogLevel.ERROR, loggingMessage + Message.NOT_FOUND.getMessage());
            return new ResponseStatusException(HttpStatus.NOT_FOUND, "Category with id " + categoryId + Message.NOT_FOUND.getMessage());
        });
        categoryRepository.delete(category);
        loggingService.log(LogLevel.INFO, loggingMessage + " name=" + category.getName() + Message.DELETE.getMessage());
    }
}
