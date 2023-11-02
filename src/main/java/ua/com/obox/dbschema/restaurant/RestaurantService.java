package ua.com.obox.dbschema.restaurant;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.hibernate.Session;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import ua.com.obox.dbschema.category.Category;
import ua.com.obox.dbschema.category.CategoryResponse;
import ua.com.obox.dbschema.dish.Dish;
import ua.com.obox.dbschema.dish.DishResponse;
import ua.com.obox.dbschema.sorting.EntityOrder;
import ua.com.obox.dbschema.sorting.EntityOrderRepository;
import ua.com.obox.dbschema.tools.FieldUpdateFunction;
import ua.com.obox.dbschema.tools.RequiredServiceHelper;
import ua.com.obox.dbschema.menu.Menu;
import ua.com.obox.dbschema.menu.MenuRepository;
import ua.com.obox.dbschema.menu.MenuResponse;
import ua.com.obox.dbschema.tenant.Tenant;
import ua.com.obox.dbschema.tenant.TenantRepository;
import ua.com.obox.dbschema.tools.Validator;
import ua.com.obox.dbschema.tools.exception.ExceptionTools;
import ua.com.obox.dbschema.tools.exception.Message;
import ua.com.obox.dbschema.tools.logging.LogLevel;
import ua.com.obox.dbschema.tools.logging.LoggingService;
import ua.com.obox.dbschema.tools.response.BadFieldsResponse;
import ua.com.obox.dbschema.tools.response.ResponseErrorMap;
import ua.com.obox.dbschema.tools.services.UpdateServiceHelper;
import ua.com.obox.dbschema.tools.translation.CheckHeader;
import ua.com.obox.dbschema.translation.Translation;
import ua.com.obox.dbschema.translation.TranslationRepository;
import ua.com.obox.dbschema.translation.assistant.CreateTranslation;
import ua.com.obox.dbschema.translation.responsebody.Content;
import ua.com.obox.dbschema.translation.responsebody.MenuTranslationEntry;
import ua.com.obox.dbschema.translation.responsebody.RestaurantTranslationEntry;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RestaurantService {
    @PersistenceContext
    private EntityManager entityManager;
    private final RestaurantRepository restaurantRepository;
    private final TenantRepository tenantRepository;
    private final MenuRepository menuRepository;
    private final EntityOrderRepository entityOrderRepository;
    private final TranslationRepository translationRepository;
    private final LoggingService loggingService;
    private final UpdateServiceHelper serviceHelper;
    private final ResourceBundle translation = ResourceBundle.getBundle("translation.messages");

    public List<MenuResponse> getAllMenusByRestaurantId(String restaurantId, String acceptLanguage) {
        String finalAcceptLanguage = CheckHeader.checkHeaderLanguage(acceptLanguage);
        ObjectMapper objectMapper = new ObjectMapper();
        AtomicReference<Content<MenuTranslationEntry>> content = new AtomicReference<>();
        AtomicReference<Translation> translation = new AtomicReference<>();

        restaurantRepository.findByRestaurantId(restaurantId).orElseThrow(() -> ExceptionTools.notFoundException(".restaurantNotFound", finalAcceptLanguage, restaurantId));

        List<Menu> menus = menuRepository.findAllByRestaurant_RestaurantIdOrderByCreatedAt(restaurantId);

        // for sorting results
        EntityOrder sortingExist = entityOrderRepository.findByEntityId(restaurantId).orElse(null);
        if (sortingExist != null) {
            List<String> MenuIdsInOrder = Arrays.stream(sortingExist.getSortedList().split(",")).toList();
            menus.sort(Comparator.comparingInt(menu -> {
                int index = MenuIdsInOrder.indexOf(menu.getMenuId());
                return index != -1 ? index : Integer.MAX_VALUE;
            }));
        }

        List<MenuResponse> responseList = menus.stream()
                .map(menu -> {
                    try {
                        translation.set(translationRepository.findAllByTranslationId(menu.getTranslationId()).orElseThrow(() ->
                                ExceptionTools.notFoundException(".translationNotFound", finalAcceptLanguage, menu.getMenuId())));
                        content.set(objectMapper.readValue(translation.get().getContent(), new TypeReference<>() {
                        }));
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }

                    return MenuResponse.builder()
                            .restaurantId(menu.getRestaurant().getRestaurantId())
                            .menuId(menu.getMenuId())
                            .translationId(menu.getTranslationId())
                            .state(menu.getState())
                            .content(content.get())
                            .build();
                })
                .collect(Collectors.toList());

        loggingService.log(LogLevel.INFO, String.format("getAllMenusByRestaurantId %s %s %d", restaurantId, Message.FIND_COUNT.getMessage(), responseList.size()));
        return responseList;
    }

    public RestaurantResponse getRestaurantById(String restaurantId, String acceptLanguage) throws JsonProcessingException {
        String finalAcceptLanguage = CheckHeader.checkHeaderLanguage(acceptLanguage);

        Restaurant restaurant = restaurantRepository.findByRestaurantId(restaurantId).orElseThrow(() -> ExceptionTools.notFoundException(".restaurantNotFound", finalAcceptLanguage, restaurantId));
        Translation translation = translationRepository.findAllByTranslationId(restaurant.getTranslationId())
                .orElseThrow(() -> ExceptionTools.notFoundException(".translationNotFound", finalAcceptLanguage, restaurantId));

        ObjectMapper objectMapper = new ObjectMapper();
        Content<RestaurantTranslationEntry> content = objectMapper.readValue(translation.getContent(), new TypeReference<>() {
        });

        loggingService.log(LogLevel.INFO, String.format("getRestaurantById %s", restaurantId));
        return RestaurantResponse.builder()
                .restaurantId(restaurant.getRestaurantId())
                .tenantId(restaurant.getTenant().getTenantId())
                .translationId(restaurant.getTranslationId())
                .content(content)
                .build();
    }

    public RestaurantResponseId createRestaurant(Restaurant request, String acceptLanguage) throws JsonProcessingException {
        String finalAcceptLanguage = CheckHeader.checkHeaderLanguage(acceptLanguage);
        Map<String, String> fieldErrors = new ResponseErrorMap<>();

        request.setTenantIdForRestaurant(request.getTenantId());

        Optional<Tenant> tenant = tenantRepository.findByTenantId(request.getTenantId());
        if (tenant.isEmpty())
            fieldErrors.put("tenant_id", String.format(translation.getString(finalAcceptLanguage + ".tenantNotFound"), request.getTenant().getTenantId()));


        Restaurant restaurant = Restaurant.builder()
                .tenant(tenant.orElse(null))
                .build();

        validateRequest(request, restaurant, finalAcceptLanguage, fieldErrors, true);

        restaurant.setCreatedAt(Instant.now().getEpochSecond());
        restaurant.setUpdatedAt(Instant.now().getEpochSecond());
        restaurantRepository.save(restaurant);

        {
            CreateTranslation<RestaurantTranslationEntry> createTranslation = new CreateTranslation<>(translationRepository);
            RestaurantTranslationEntry entry = new RestaurantTranslationEntry(restaurant.getName(), restaurant.getAddress());
            Translation translation = createTranslation
                    .create(restaurant.getRestaurantId(), "restaurant", request.getLanguage(), entry);
            restaurant.setTranslationId(translation.getTranslationId());
        }

        loggingService.log(LogLevel.INFO, String.format("createRestaurant %s UUID=%s %s", request.getName(), restaurant.getRestaurantId(), Message.CREATE.getMessage()));
        return RestaurantResponseId.builder()
                .restaurantId(restaurant.getRestaurantId())
                .build();
    }

    public void patchRestaurantById(String restaurantId, Restaurant request, String acceptLanguage) throws JsonProcessingException {
        String finalAcceptLanguage = CheckHeader.checkHeaderLanguage(acceptLanguage);
        Map<String, String> fieldErrors = new ResponseErrorMap<>();

        try (Session session = entityManager.unwrap(Session.class)) {
            Restaurant restaurant = restaurantRepository.findByRestaurantId(restaurantId).orElseThrow(() -> ExceptionTools.notFoundException(".restaurantNotFound", finalAcceptLanguage, restaurantId));
            Translation translation = translationRepository.findAllByTranslationId(restaurant.getTranslationId())
                    .orElseThrow(() -> ExceptionTools.notFoundException(".translationNotFound", finalAcceptLanguage, restaurantId));

            session.evict(restaurant); // unbind the session

            validateRequest(request, restaurant, finalAcceptLanguage, fieldErrors, false);
            updateTranslation(restaurant, request.getLanguage(), translation, finalAcceptLanguage);

            restaurant.setUpdatedAt(Instant.now().getEpochSecond());
            restaurantRepository.save(restaurant);
        }
        loggingService.log(LogLevel.INFO, String.format("patchRestaurantById %s %s", restaurantId, Message.UPDATE.getMessage()));
    }

    public void deleteRestaurantById(String restaurantId, String acceptLanguage) {
        String finalAcceptLanguage = CheckHeader.checkHeaderLanguage(acceptLanguage);

        Restaurant restaurant = restaurantRepository.findByRestaurantId(restaurantId).orElseThrow(() -> ExceptionTools.notFoundException(".restaurantNotFound", finalAcceptLanguage, restaurantId));

        restaurantRepository.delete(restaurant);
        loggingService.log(LogLevel.INFO, String.format("restaurantId %s NAME=%s %s", restaurantId, restaurant.getName(), Message.DELETE.getMessage()));
    }

    public List<MenuResponse> getAllMenusCategoriesDishesByRestaurantId(String restaurantId, String acceptLanguage) {
        String finalAcceptLanguage = CheckHeader.checkHeaderLanguage(acceptLanguage);

        ObjectMapper objectMapper = new ObjectMapper();
        AtomicReference<Content> content = new AtomicReference<>();
        AtomicReference<Translation> translation = new AtomicReference<>();

        restaurantRepository.findByRestaurantId(restaurantId).orElseThrow(() -> ExceptionTools.notFoundException(".restaurantNotFound", finalAcceptLanguage, restaurantId));

        List<Menu> menus = menuRepository.findAllByRestaurant_RestaurantIdOrderByCreatedAt(restaurantId);

        // for sorting results
        EntityOrder sortingExist = entityOrderRepository.findByEntityId(restaurantId).orElse(null);
        if (sortingExist != null) {
            List<String> MenuIdsInOrder = Arrays.stream(sortingExist.getSortedList().split(",")).toList();
            menus.sort(Comparator.comparingInt(menu -> {
                int index = MenuIdsInOrder.indexOf(menu.getMenuId());
                return index != -1 ? index : Integer.MAX_VALUE;
            }));
        }

        List<MenuResponse> responseList = menus.stream()
                .map(menu -> {
                    try {
                        translation.set(translationRepository.findAllByTranslationId(menu.getTranslationId()).orElseThrow(() ->
                                ExceptionTools.notFoundException(".translationNotFound", finalAcceptLanguage, menu.getMenuId())));
                        content.set(objectMapper.readValue(translation.get().getContent(), new TypeReference<>() {
                        }));
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }

                    MenuResponse menuResponse = new MenuResponse();
                    menuResponse.setRestaurantId(menu.getRestaurant().getRestaurantId());
                    menuResponse.setMenuId(menu.getMenuId());
                    menuResponse.setTranslationId(menu.getTranslationId());
                    menuResponse.setState(menu.getState());
                    menuResponse.setContent(content.get());

                    //start menu sorting
                    EntityOrder categoryExist = entityOrderRepository.findByEntityId(menu.getMenuId()).orElse(null);
                    List<String> CategoryIdsInOrder = new ArrayList<>();
                    if (categoryExist != null) {
                        CategoryIdsInOrder = Arrays.stream(categoryExist.getSortedList().split(",")).toList();
                    }
                    List<String> finalCategoryIdsInOrder = CategoryIdsInOrder;
                    menu.getCategories().sort(Comparator.comparing(Category::getCreatedAt).reversed());
                    menu.getCategories().sort(Comparator.comparingInt(category -> {
                        int index = finalCategoryIdsInOrder.indexOf(category.getCategoryId());
                        return index != -1 ? index : Integer.MAX_VALUE;
                    }));
                    //finish menu sorting

                    List<CategoryResponse> categoryResponseList = menu.getCategories().stream()
                            .map(category -> {
                                try {
                                    translation.set(translationRepository.findAllByTranslationId(category.getTranslationId()).orElseThrow(() ->
                                            ExceptionTools.notFoundException(".translationNotFound", finalAcceptLanguage, category.getCategoryId())));
                                    content.set(objectMapper.readValue(translation.get().getContent(), new TypeReference<>() {
                                    }));
                                } catch (JsonProcessingException e) {
                                    throw new RuntimeException(e);
                                }

                                CategoryResponse categoryResponse = CategoryResponse.builder()
                                        .menuId(category.getMenu().getMenuId())
                                        .categoryId(category.getCategoryId())
                                        .translationId(category.getTranslationId())
                                        .state(category.getState())
                                        .content(content.get())
                                        .build();

                                //start dish sorting
                                EntityOrder dishExist = entityOrderRepository.findByEntityId(category.getCategoryId()).orElse(null);
                                List<String> DishIdsInOrder = new ArrayList<>();
                                if (dishExist != null) {
                                    DishIdsInOrder = Arrays.stream(dishExist.getSortedList().split(",")).toList();
                                }
                                List<String> finalDishIdsInOrder = DishIdsInOrder;
                                category.getDishes().sort(Comparator.comparing(Dish::getCreatedAt).reversed());
                                category.getDishes().sort(Comparator.comparingInt(dish -> {
                                    int index = finalDishIdsInOrder.indexOf(dish.getDishId());
                                    return index != -1 ? index : Integer.MAX_VALUE;
                                }));
                                //finish dish sorting

                                List<DishResponse> dishResponseList = category.getDishes().stream()
                                        .map(dish -> {
                                            try {
                                                translation.set(translationRepository.findAllByTranslationId(dish.getTranslationId()).orElseThrow(() ->
                                                        ExceptionTools.notFoundException(".translationNotFound", finalAcceptLanguage, dish.getDishId())));
                                                content.set(objectMapper.readValue(translation.get().getContent(), new TypeReference<>() {
                                                }));
                                            } catch (JsonProcessingException e) {
                                                throw new RuntimeException(e);
                                            }
                                            DishResponse dishResponse = DishResponse.builder()
                                                    .categoryId(dish.getCategory().getCategoryId())
                                                    .dishId(dish.getDishId())
                                                    .translationId(dish.getTranslationId())
                                                    .price(dish.getPrice())
                                                    .specialPrice(dish.getSpecialPrice())
                                                    .cookingTime(dish.getCookingTime())
                                                    .calories(dish.getCalories())
                                                    .weight(dish.getWeight())
                                                    .weightUnit(dish.getWeightUnit())
                                                    .inStock(dish.getInStock())
                                                    .state(dish.getState())
                                                    .image(dish.getImage())
                                                    .content(content.get())
                                                    .build();
                                            return dishResponse;
                                        }).collect(Collectors.toList());

                                categoryResponse.setDishes(dishResponseList);
                                return categoryResponse;
                            }).collect(Collectors.toList());

                    menuResponse.setCategories(categoryResponseList);
                    return menuResponse;

                }).collect(Collectors.toList());

        loggingService.log(LogLevel.INFO, String.format("getAllMenusCategoriesDishesByRestaurantId %s ", restaurantId));
        return responseList;
    }

    private void validateRequest(Restaurant request, Restaurant restaurant, String finalAcceptLanguage, Map<String, String> fieldErrors, boolean required) {
        fieldErrors.put("language", Validator.validateLanguage(request.getLanguage(), finalAcceptLanguage));
        updateField(request.getName(), required, restaurant, fieldErrors, "name",
                (name) -> serviceHelper.updateNameField(restaurant::setName, name, finalAcceptLanguage));
        fieldErrors.put("address", serviceHelper.updateVarcharField(restaurant::setAddress, request.getAddress(), "address", finalAcceptLanguage));

        if (fieldErrors.size() > 0)
            throw new BadFieldsResponse(HttpStatus.BAD_REQUEST, fieldErrors);
    }

    private <T> void updateField(T value, boolean required, Restaurant restaurant, Map<String, String> fieldErrors, String fieldName, FieldUpdateFunction<T> updateFunction) {
        if (value != null || required) {
            String error = updateFunction.updateField(value);
            if (error != null) {
                fieldErrors.put(fieldName, error);
            }
        }
    }

    private void updateTranslation(Restaurant restaurant, String language, Translation translation, String finalAcceptLanguage) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        TypeReference<Content<RestaurantTranslationEntry>> typeReference = new TypeReference<>() {
        };
        Content<RestaurantTranslationEntry> content = objectMapper.readValue(translation.getContent(), typeReference);
        Map<String, RestaurantTranslationEntry> languagesMap = content.getContent();
        if (languagesMap.get(language) != null) {
            if (restaurant.getName() == null)
                restaurant.setName(languagesMap.get(language).getName());
            if (restaurant.getAddress() == null) {
                restaurant.setAddress(content.getContent().get(language).getAddress());
            } else if (restaurant.getAddress().equals("")) {
                restaurant.setAddress(null);
            }
        }
        languagesMap.put(language, new RestaurantTranslationEntry(restaurant.getName(), restaurant.getAddress()));
        translation.setContent(objectMapper.writeValueAsString(content));
        translation.setUpdatedAt(Instant.now().getEpochSecond());
    }
}