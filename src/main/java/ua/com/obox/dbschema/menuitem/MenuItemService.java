package ua.com.obox.dbschema.menuitem;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import ua.com.obox.dbschema.category.Category;
import ua.com.obox.dbschema.category.CategoryRepository;
import ua.com.obox.dbschema.tools.State;
import ua.com.obox.dbschema.tools.Validator;
import ua.com.obox.dbschema.tools.exception.ExceptionTools;
import ua.com.obox.dbschema.tools.exception.Message;
import ua.com.obox.dbschema.tools.logging.LogLevel;
import ua.com.obox.dbschema.tools.logging.LoggingService;
import ua.com.obox.dbschema.tools.ftp.UploadItemImageFTP;

@Service
@RequiredArgsConstructor
public class MenuItemService {

    private final MenuItemRepository itemRepository;
    private final CategoryRepository categoryRepository;
    private final LoggingService loggingService;
    private final UploadItemImageFTP itemImageFTP;
    private String loggingMessage;

    public MenuItemResponse getItemById(String itemId) {
        loggingMessage = ExceptionTools.generateLoggingMessage("getItemById", itemId);
        var itemInfo = itemRepository.findByItemId(itemId);
        MenuItem item = itemInfo.orElseThrow(() -> {
            loggingService.log(LogLevel.ERROR, loggingMessage + Message.NOT_FOUND.getMessage());
            return new ResponseStatusException(HttpStatus.NOT_FOUND, "Item with id " + itemId + Message.NOT_FOUND.getMessage());
        });

        if (item.getState().equals(State.DISABLE)) {
            loggingService.log(LogLevel.ERROR, loggingMessage + Message.HIDDEN.getMessage());
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Item with id " + itemId + Message.HIDDEN.getMessage());
        }

        loggingService.log(LogLevel.INFO, loggingMessage);
        return MenuItemResponse.builder()
                .itemId(item.getItemId())
                .categoryId(item.getCategory().getCategoryId())
                .name(item.getName())
                .description(item.getDescription())
                .price(item.getPrice())
                .imageUrl(item.getImageUrl())
                .state(item.getState())
                .build();
    }

    public MenuItemResponseId createItem(MenuItem request) {
        String imageServerDns = "https://img.obox.com.ua/";
        String image = "";
        if (request.getImage() != null) {
            image = request.getImage();
        }
        loggingMessage = ExceptionTools.generateLoggingMessage("createItem", request.getCategory_id());
        request.setCategoryIdForMenuItem(request.getCategory_id());
        Category category = categoryRepository.findByCategoryId(request.getCategory().getCategoryId()).orElseThrow(() -> {
            loggingService.log(LogLevel.ERROR, loggingMessage + Message.CATEGORY_NOT_FOUND.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Category with id " + request.getCategory().getCategoryId() + Message.NOT_FOUND.getMessage(), null);
        });
        MenuItem item = MenuItem.builder()
                .name(request.getName().trim()) // delete whitespaces
                .description(request.getDescription().trim())
                .price(request.getPrice())
                .category(category)
                .calories(request.getCalories())
                .weight(request.getWeight())
                .state(request.getState())
                .build();
        itemRepository.save(item);
        if (!image.isEmpty() && Validator.validateImage(image, loggingService)) {
            item.setImageUrl(imageServerDns + itemImageFTP.uploadImage(request.getImage(), item.getItemId(), loggingService));
            itemRepository.save(item);
        }
        loggingService.log(LogLevel.INFO, loggingMessage + " id=" + item.getItemId() + Message.CREATE.getMessage());
        return MenuItemResponseId.builder()
                .itemId(item.getItemId())
                .build();
    }

    public void patchItemById(String itemId, MenuItem request) {
        String image = "";
        System.out.println("input state" + request.getState());
        if (request.getImage() != null) {
            image = request.getImage();
        }
        loggingMessage = ExceptionTools.generateLoggingMessage("patchItemById", itemId);
        var itemInfo = itemRepository.findByItemId(itemId);
        MenuItem item = itemInfo.orElseThrow(() -> {
            loggingService.log(LogLevel.ERROR, loggingMessage + Message.NOT_FOUND.getMessage());
            return new ResponseStatusException(HttpStatus.NOT_FOUND, "Item with id " + itemId + Message.NOT_FOUND.getMessage());
        });

        if (!image.isEmpty() && Validator.validateImage(image, loggingService)) {
            itemImageFTP.deleteImage(item.getImageUrl(), loggingService); // delete old image
            item.setImageUrl(itemImageFTP.uploadImage(request.getImage(), item.getItemId(), loggingService)); // upload new image
        }
        item.setCategoryIdForMenuItem(request.getCategory_id()); // set new category id;
        item.setName(request.getName().trim()); // delete whitespaces
        item.setDescription(request.getDescription().trim());
        item.setPrice(request.getPrice());
        item.setCalories(request.getCalories());
        item.setWeight(request.getWeight());
        item.setCategory(item.getCategory());
        if (!request.getState().equals(item.getState())) {
            item.setState(request.getState());
        }
        itemRepository.save(item);
        loggingService.log(LogLevel.INFO, loggingMessage + Message.UPDATE.getMessage());
    }

    public void deleteItemById(String itemId) {
        loggingMessage = ExceptionTools.generateLoggingMessage("deleteItemById", itemId);
        var itemInfo = itemRepository.findByItemId(itemId);
        MenuItem item = itemInfo.orElseThrow(() -> {
            loggingService.log(LogLevel.ERROR, loggingMessage + Message.NOT_FOUND.getMessage());
            return new ResponseStatusException(HttpStatus.NOT_FOUND, "Item with id " + itemId + Message.NOT_FOUND.getMessage());
        });
        itemRepository.delete(item);
        loggingService.log(LogLevel.INFO, loggingMessage + " name=" + item.getName() + Message.DELETE.getMessage());
    }

}
