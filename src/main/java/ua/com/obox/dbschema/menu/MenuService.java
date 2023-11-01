package ua.com.obox.dbschema.menu;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import ua.com.obox.dbschema.category.Category;
import ua.com.obox.dbschema.category.CategoryRepository;
import ua.com.obox.dbschema.category.CategoryResponse;
import ua.com.obox.dbschema.restaurant.Restaurant;
import ua.com.obox.dbschema.restaurant.RestaurantRepository;
import ua.com.obox.dbschema.sorting.EntityOrder;
import ua.com.obox.dbschema.sorting.EntityOrderRepository;
import ua.com.obox.dbschema.tools.FieldUpdateFunction;
import ua.com.obox.dbschema.tools.Validator;
import ua.com.obox.dbschema.tools.exception.ExceptionTools;
import ua.com.obox.dbschema.tools.response.BadFieldsResponse;
import ua.com.obox.dbschema.tools.response.ResponseErrorMap;
import ua.com.obox.dbschema.tools.services.UpdateServiceHelper;
import ua.com.obox.dbschema.tools.exception.Message;
import ua.com.obox.dbschema.tools.logging.LogLevel;
import ua.com.obox.dbschema.tools.logging.LoggingService;
import ua.com.obox.dbschema.tools.translation.CheckHeader;
import ua.com.obox.dbschema.translation.Translation;
import ua.com.obox.dbschema.translation.TranslationRepository;
import ua.com.obox.dbschema.translation.assistant.CreateTranslation;
import ua.com.obox.dbschema.translation.assistant.ExistEntity;
import ua.com.obox.dbschema.translation.responsebody.CategoryTranslationEntry;
import ua.com.obox.dbschema.translation.responsebody.Content;
import ua.com.obox.dbschema.translation.responsebody.MenuTranslationEntry;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MenuService {
    private final MenuRepository menuRepository;
    private final RestaurantRepository restaurantRepository;
    private final CategoryRepository categoryRepository;
    private final EntityOrderRepository entityOrderRepository;
    private final TranslationRepository translationRepository;
    private final LoggingService loggingService;
    private final UpdateServiceHelper serviceHelper;
    private final ResourceBundle translationContent = ResourceBundle.getBundle("translation.messages");

    public List<CategoryResponse> getAllCategoriesByMenuId(String menuId, String acceptLanguage) {
        String finalAcceptLanguage = CheckHeader.checkHeaderLanguage(acceptLanguage);
        ObjectMapper objectMapper = new ObjectMapper();
        AtomicReference<Content<CategoryTranslationEntry>> content = new AtomicReference<>();
        AtomicReference<Translation> translation = new AtomicReference<>();

        menuRepository.findByMenuId(menuId).orElseThrow(() -> ExceptionTools.notFoundException(".menuNotFound", finalAcceptLanguage, menuId));

        List<Category> categories = categoryRepository.findAllByMenu_MenuId(menuId);

        // for sorting results
        EntityOrder sortingExist = entityOrderRepository.findByEntityId(menuId).orElse(null);
        if (sortingExist != null) {
            List<String> categoryIdsInOrder = Arrays.stream(sortingExist.getSortedList().split(",")).toList();
            categories.sort(Comparator.comparingInt(category -> {
                int index = categoryIdsInOrder.indexOf(category.getCategoryId());
                return index != -1 ? index : Integer.MAX_VALUE;
            }));
        }

        List<CategoryResponse> responseList = categories.stream()
                .map(category -> {
                    try {
                        translation.set(translationRepository.findAllByTranslationId(category.getTranslationId()).orElseThrow(() ->
                                ExceptionTools.notFoundException(".translationNotFound", finalAcceptLanguage, category.getCategoryId())));
                        content.set(objectMapper.readValue(translation.get().getContent(), new TypeReference<>() {
                        }));
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }

                    return CategoryResponse.builder()
                            .menuId(category.getMenu().getMenuId())
                            .categoryId(category.getCategoryId())
                            .translationId(category.getTranslationId())
                            .state(category.getState())
                            .content(content.get())
                            .build();
                })
                .collect(Collectors.toList());
//
//                .map(category -> CategoryResponse.builder()
//                        .categoryId(category.getCategoryId())
//                        .menuId(category.getMenu().getMenuId())
//                        .name(category.getName())
//                        .description(category.getDescription())
//                        .state(category.getState())
//                        .build()).collect(Collectors.toList());

        loggingService.log(LogLevel.INFO, String.format("getAllCategoriesByMenuId %s %s %d", menuId, Message.FIND_COUNT.getMessage(), responseList.size()));
        return responseList;
    }

    public MenuResponse getMenuById(String menuId, String acceptLanguage) throws JsonProcessingException {
        String finalAcceptLanguage = CheckHeader.checkHeaderLanguage(acceptLanguage);

        Menu menu = menuRepository.findByMenuId(menuId).orElseThrow(() -> ExceptionTools.notFoundException(".menuNotFound", finalAcceptLanguage, menuId));
        Translation translation = translationRepository.findAllByTranslationId(menu.getTranslationId())
                .orElseThrow(() -> ExceptionTools.notFoundException(".translationNotFound", finalAcceptLanguage, menuId));

        ObjectMapper objectMapper = new ObjectMapper();
        Content<MenuTranslationEntry> content = objectMapper.readValue(translation.getContent(), new TypeReference<>() {
        });

        loggingService.log(LogLevel.INFO, String.format("getMenuById %s", menuId));
        return MenuResponse.builder()
                .restaurantId(menu.getRestaurant().getRestaurantId())
                .menuId(menu.getMenuId())
                .translationId(menu.getTranslationId())
                .content(content)
                .state(menu.getState())
                .build();
    }

    public MenuResponseId createMenu(Menu request, String acceptLanguage) throws JsonProcessingException {
        String finalAcceptLanguage = CheckHeader.checkHeaderLanguage(acceptLanguage);
        Map<String, String> fieldErrors = new ResponseErrorMap<>();

        Optional<Restaurant> restaurant = restaurantRepository.findByRestaurantId(request.getRestaurantId());
        if (restaurant.isEmpty())
            fieldErrors.put("restaurant_id", String.format(translationContent.getString(finalAcceptLanguage + ".restaurantNotFound"), request.getRestaurantId()));

        Menu menu = Menu.builder()
                .restaurant(restaurant.orElse(null))
                .build();

        validateRequest(request, menu, finalAcceptLanguage, fieldErrors, true);

        menu.setCreatedAt(Instant.now().getEpochSecond());
        menu.setUpdatedAt(Instant.now().getEpochSecond());
        menuRepository.save(menu);

        {
            CreateTranslation<MenuTranslationEntry> createTranslation = new CreateTranslation<>(translationRepository);
            MenuTranslationEntry entry = new MenuTranslationEntry(menu.getName());
            Translation translation = createTranslation
                    .create(menu.getMenuId(), "menu", request.getLanguage(), entry);
            menu.setTranslationId(translation.getTranslationId());
        }

        loggingService.log(LogLevel.INFO, String.format("createMenu %s UUID=%s %s", request.getName(), menu.getMenuId(), Message.CREATE.getMessage()));

        return MenuResponseId.builder()
                .menuId(menu.getMenuId())
                .build();
    }

    public void patchMenuById(String menuId, Menu request, String acceptLanguage) throws JsonProcessingException {
        String finalAcceptLanguage = CheckHeader.checkHeaderLanguage(acceptLanguage);
        Map<String, String> fieldErrors = new ResponseErrorMap<>();

        Menu menu = menuRepository.findByMenuId(menuId).orElseThrow(() -> ExceptionTools.notFoundException(".menuNotFound", finalAcceptLanguage, menuId));
        Translation translation = translationRepository.findAllByTranslationId(menu.getTranslationId())
                .orElseThrow(() -> ExceptionTools.notFoundException(".translationNotFound", finalAcceptLanguage, menuId));

        validateRequest(request, menu, finalAcceptLanguage, fieldErrors, false);
        updateTranslation(menu.getName(), request.getLanguage(), translation);

        menu.setUpdatedAt(Instant.now().getEpochSecond());
        menuRepository.save(menu);
        loggingService.log(LogLevel.INFO, String.format("patchMenuById %s %s", menuId, Message.UPDATE.getMessage()));
    }


    public void deleteMenuById(String menuId, String acceptLanguage) {
        String finalAcceptLanguage = CheckHeader.checkHeaderLanguage(acceptLanguage);
        Menu menu = menuRepository.findByMenuId(menuId).orElseThrow(() -> ExceptionTools.notFoundException(".menuNotFound", finalAcceptLanguage, menuId));
        menuRepository.delete(menu);
        loggingService.log(LogLevel.INFO, String.format("deleteMenuById %s NAME=%s %s", menuId, menu.getName(), Message.DELETE.getMessage()));
    }

    private void validateRequest(Menu request, Menu menu, String finalAcceptLanguage, Map<String, String> fieldErrors, boolean required) {
        fieldErrors.put("language", Validator.validateLanguage(request.getLanguage(), finalAcceptLanguage));

        updateField(request.getName(), required, menu, fieldErrors, "name",
                (name) -> serviceHelper.updateNameField(menu::setName, name, finalAcceptLanguage), finalAcceptLanguage);

        updateField(request.getState(), required, menu, fieldErrors, "state",
                (state) -> serviceHelper.updateState(menu::setState, state, finalAcceptLanguage), finalAcceptLanguage);

        if (fieldErrors.size() > 0)
            throw new BadFieldsResponse(HttpStatus.BAD_REQUEST, fieldErrors);
    }

    private <T> void updateField(T value, boolean required, Menu menu, Map<String, String> fieldErrors, String fieldName, FieldUpdateFunction<T> updateFunction, String finalAcceptLanguage) {
        if (value != null || required) {
            if (menu.getRestaurant() != null) {
                List<Menu> sameParent = menuRepository.findAllByRestaurant_RestaurantId(menu.getRestaurant().getRestaurantId());
                sameParent.remove(menu);
                ExistEntity<MenuTranslationEntry> existEntity = new ExistEntity<>(translationRepository);
                existEntity.checkExistEntity(Validator.removeExtraSpaces((String) value), sameParent, finalAcceptLanguage, fieldErrors);
            }
            String error = updateFunction.updateField(value);
            if (error != null) {
                fieldErrors.put(fieldName, error);
            }
        }
    }

    private void updateTranslation(String name, String language, Translation translation) throws JsonProcessingException {
        if (name != null) {
            ObjectMapper objectMapper = new ObjectMapper();
            TypeReference<Content<MenuTranslationEntry>> typeReference = new TypeReference<>() {
            };
            Content<MenuTranslationEntry> content = objectMapper.readValue(translation.getContent(), typeReference);
            Map<String, MenuTranslationEntry> languagesMap = content.getContent();
            languagesMap.put(language, new MenuTranslationEntry(name));
            translation.setContent(objectMapper.writeValueAsString(content));
            translation.setUpdatedAt(Instant.now().getEpochSecond());
        }
    }
}