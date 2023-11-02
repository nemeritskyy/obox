package ua.com.obox.dbschema.tenant;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import ua.com.obox.dbschema.tools.RequiredServiceHelper;
import ua.com.obox.dbschema.restaurant.Restaurant;
import ua.com.obox.dbschema.restaurant.RestaurantRepository;
import ua.com.obox.dbschema.restaurant.RestaurantResponse;
import ua.com.obox.dbschema.tools.Validator;
import ua.com.obox.dbschema.tools.exception.ExceptionTools;
import ua.com.obox.dbschema.tools.response.ResponseErrorMap;
import ua.com.obox.dbschema.tools.response.BadFieldsResponse;
import ua.com.obox.dbschema.tools.services.UpdateServiceHelper;
import ua.com.obox.dbschema.tools.State;
import ua.com.obox.dbschema.tools.exception.Message;
import ua.com.obox.dbschema.tools.logging.LogLevel;
import ua.com.obox.dbschema.tools.logging.LoggingService;
import ua.com.obox.dbschema.tools.translation.CheckHeader;
import ua.com.obox.dbschema.translation.Translation;
import ua.com.obox.dbschema.translation.TranslationRepository;
import ua.com.obox.dbschema.translation.assistant.CreateTranslation;
import ua.com.obox.dbschema.translation.responsebody.Content;
import ua.com.obox.dbschema.translation.responsebody.RestaurantTranslationEntry;
import ua.com.obox.dbschema.translation.responsebody.TenantTranslationEntry;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TenantService {
    private final TenantRepository tenantRepository;
    private final RestaurantRepository restaurantRepository;
    private final TranslationRepository translationRepository;
    private final LoggingService loggingService;
    private final UpdateServiceHelper serviceHelper;
    private final RequiredServiceHelper requiredServiceHelper;

    public List<RestaurantResponse> getAllRestaurantsByTenantId(String tenantId, String acceptLanguage) {
        String finalAcceptLanguage = CheckHeader.checkHeaderLanguage(acceptLanguage);
        ObjectMapper objectMapper = new ObjectMapper();
        AtomicReference<Content<RestaurantTranslationEntry>> content = new AtomicReference<>();
        AtomicReference<Translation> translation = new AtomicReference<>();

        tenantRepository.findByTenantId(tenantId).orElseThrow(() -> ExceptionTools.notFoundException(".tenantNotFound", finalAcceptLanguage, tenantId));

        List<Restaurant> restaurants = restaurantRepository.findAllByTenant_TenantId(tenantId);

        List<RestaurantResponse> responseList = restaurants.stream()
                .map(restaurant -> {
                    try {
                        translation.set(translationRepository.findAllByTranslationId(restaurant.getTranslationId()).orElseThrow(() ->
                                ExceptionTools.notFoundException(".translationNotFound", finalAcceptLanguage, restaurant.getRestaurantId())));
                        content.set(objectMapper.readValue(translation.get().getContent(), new TypeReference<>() {
                        }));
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }

                    return RestaurantResponse.builder()
                            .tenantId(restaurant.getTenant().getTenantId())
                            .restaurantId(restaurant.getRestaurantId())
                            .translationId(restaurant.getTranslationId())
                            .content(content.get())
                            .build();
                })
                .collect(Collectors.toList());

        loggingService.log(LogLevel.INFO, String.format("getAllRestaurantsByTenantId %s %s %d", tenantId, Message.FIND_COUNT.getMessage(), responseList.size()));
        return responseList;
    }

    public TenantResponse getTenantById(String tenantId, String acceptLanguage) throws JsonProcessingException {
        String finalAcceptLanguage = CheckHeader.checkHeaderLanguage(acceptLanguage);

        Tenant tenant = tenantRepository.findByTenantId(tenantId).orElseThrow(() -> ExceptionTools.notFoundException(".tenantNotFound", finalAcceptLanguage, tenantId));
        Translation translation = translationRepository.findAllByTranslationId(tenant.getTranslationId())
                .orElseThrow(() -> ExceptionTools.notFoundException(".translationNotFound", finalAcceptLanguage, tenantId));

        ObjectMapper objectMapper = new ObjectMapper();
        Content<TenantTranslationEntry> content = objectMapper.readValue(translation.getContent(), new TypeReference<>() {
        });

        if (tenant.getState().equals(State.DISABLED))
            ExceptionTools.forbiddenResponse(finalAcceptLanguage, tenantId);

        loggingService.log(LogLevel.INFO, String.format("getTenantById %s", tenantId));
        return TenantResponse.builder()
                .tenantId(tenant.getTenantId())
                .translationId(tenant.getTranslationId())
                .content(content)
                .build();
    }

    public TenantResponseId createTenant(Tenant request, String acceptLanguage) throws JsonProcessingException {
        String finalAcceptLanguage = CheckHeader.checkHeaderLanguage(acceptLanguage);
        Map<String, String> fieldErrors = new ResponseErrorMap<>();

        Tenant tenant = Tenant.builder()
                .state(State.ENABLED)
                .build();

        validateRequest(request, tenant, finalAcceptLanguage, fieldErrors, true);

        tenant.setCreatedAt(Instant.now().getEpochSecond());
        tenant.setUpdatedAt(Instant.now().getEpochSecond());
        tenantRepository.save(tenant);

        {
            CreateTranslation<TenantTranslationEntry> createTranslation = new CreateTranslation<>(translationRepository);
            TenantTranslationEntry entry = new TenantTranslationEntry(tenant.getName());
            Translation translation = createTranslation
                    .create(tenant.getTenantId(), "tenant", request.getLanguage(), entry);
            tenant.setTranslationId(translation.getTranslationId());
        }

        loggingService.log(LogLevel.INFO, String.format("createTenant %s UUID=%s %s", request.getName(), tenant.getTenantId(), Message.CREATE.getMessage()));
        return TenantResponseId.builder()
                .tenantId(tenant.getTenantId())
                .build();
    }

    public void patchTenantById(String tenantId, Tenant request, String acceptLanguage) throws JsonProcessingException {
        String finalAcceptLanguage = CheckHeader.checkHeaderLanguage(acceptLanguage);
        Map<String, String> fieldErrors = new ResponseErrorMap<>();

        Tenant tenant = tenantRepository.findByTenantId(tenantId).orElseThrow(() -> ExceptionTools.notFoundException(".tenantNotFound", finalAcceptLanguage, tenantId));
        Translation translation = translationRepository.findAllByTranslationId(tenant.getTranslationId())
                .orElseThrow(() -> ExceptionTools.notFoundException(".translationNotFound", finalAcceptLanguage, tenantId));

        validateRequest(request, tenant, finalAcceptLanguage, fieldErrors, false);
        updateTranslation(tenant, request.getLanguage(), translation);

        tenant.setUpdatedAt(Instant.now().getEpochSecond());
        tenantRepository.save(tenant);
        loggingService.log(LogLevel.INFO, String.format("patchTenantById %s %s", tenantId, Message.UPDATE.getMessage()));
    }

    public void deleteTenantById(String tenantId, boolean forceDelete, String acceptLanguage) {
        String finalAcceptLanguage = CheckHeader.checkHeaderLanguage(acceptLanguage);

        Tenant tenant = tenantRepository.findByTenantId(tenantId).orElseThrow(() -> ExceptionTools.notFoundException(".tenantNotFound", finalAcceptLanguage, tenantId));

        if (!forceDelete) {
            tenant.setState(State.DISABLED);
            tenantRepository.save(tenant);
        } else {
            tenantRepository.delete(tenant);
        }

        loggingService.log(LogLevel.INFO, String.format("deleteTenantById %s NAME=%s %s", tenantId, tenant.getName(), Message.DELETE.getMessage()));
    }

    private void validateRequest(Tenant request, Tenant tenant, String finalAcceptLanguage, Map<String, String> fieldErrors, boolean required) {
        fieldErrors.put("language", Validator.validateLanguage(request.getLanguage(), finalAcceptLanguage));
        if (required) {
            fieldErrors.put("name", serviceHelper.updateNameField(tenant::setName, request.getName(), finalAcceptLanguage));
        } else {
            fieldErrors.put("name", requiredServiceHelper.updateNameIfNeeded(request.getName(), tenant, finalAcceptLanguage));
        }
        if (fieldErrors.size() > 0)
            throw new BadFieldsResponse(HttpStatus.BAD_REQUEST, fieldErrors);
    }

    private void updateTranslation(Tenant tenant, String language, Translation translation) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        TypeReference<Content<TenantTranslationEntry>> typeReference = new TypeReference<>() {
        };
        Content<TenantTranslationEntry> content = objectMapper.readValue(translation.getContent(), typeReference);
        Map<String, TenantTranslationEntry> languagesMap = content.getContent();

        languagesMap.put(language, new TenantTranslationEntry(tenant.getName()));
        translation.setContent(objectMapper.writeValueAsString(content));
        translation.setUpdatedAt(Instant.now().getEpochSecond());

    }
}