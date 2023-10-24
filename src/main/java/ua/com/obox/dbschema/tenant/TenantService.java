package ua.com.obox.dbschema.tenant;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import ua.com.obox.dbschema.language.Language;
import ua.com.obox.dbschema.language.LanguageRepository;
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
import ua.com.obox.dbschema.translation.responsebody.Content;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TenantService {
    private final TenantRepository tenantRepository;
    private final RestaurantRepository restaurantRepository;
    private final TranslationRepository translationRepository;
    private final LanguageRepository languageRepository;
    private final LoggingService loggingService;
    private final UpdateServiceHelper serviceHelper;
    private final RequiredServiceHelper requiredServiceHelper;

    public List<RestaurantResponse> getAllRestaurantsByTenantId(String tenantId, String acceptLanguage) {
        String finalAcceptLanguage = CheckHeader.checkHeaderLanguage(acceptLanguage);

        var tenantInfo = tenantRepository.findByTenantId(tenantId);

        tenantInfo.orElseThrow(() -> {
            ExceptionTools.notFoundResponse(".tenantNotFound", finalAcceptLanguage, tenantId);
            return null;
        });

        List<Restaurant> restaurants = restaurantRepository.findAllByTenant_TenantId(tenantId);

        List<RestaurantResponse> responseList = restaurants.stream()
                .map(restaurant -> RestaurantResponse.builder()
                        .restaurantId(restaurant.getRestaurantId())
                        .address(restaurant.getAddress())
                        .name(restaurant.getName())
                        .tenantId(restaurant.getTenant().getTenantId())
                        .build())
                .collect(Collectors.toList());

        loggingService.log(LogLevel.INFO, String.format("getAllRestaurantsByTenantId %s %s %d", tenantId, Message.FIND_COUNT.getMessage(), responseList.size()));
        return responseList;
    }

    public TenantResponse getTenantById(String tenantId, String acceptLanguage) throws JsonProcessingException {
        String finalAcceptLanguage = CheckHeader.checkHeaderLanguage(acceptLanguage);

        var tenantInfo = tenantRepository.findByTenantId(tenantId);

        Tenant tenant = tenantInfo.orElseThrow(() -> {
            ExceptionTools.notFoundResponse(".tenantNotFound", finalAcceptLanguage, tenantId);
            return null;
        });

        Translation translationFromDB = translationRepository.findAllByTranslationId(tenant.getTranslationId()).orElseThrow(() -> {
            ExceptionTools.notFoundResponse(".translationNotFound", finalAcceptLanguage, tenantId);
            return null;
        });

        ObjectMapper objectMapper = new ObjectMapper();
        Content content = objectMapper.readValue(translationFromDB.getContent(), Content.class);

        if (tenant.getState().equals(State.DISABLED))
            ExceptionTools.forbiddenResponse(finalAcceptLanguage, tenantId);

        loggingService.log(LogLevel.INFO, String.format("getTenantById %s", tenantId));
        return TenantResponse.builder()
                .tenantId(tenant.getTenantId())
                .content(content)
                .build();
    }

    public TenantResponseId createTenant(Tenant request, String acceptLanguage) throws JsonProcessingException {
        String finalAcceptLanguage = CheckHeader.checkHeaderLanguage(acceptLanguage);
        Map<String, String> fieldErrors = new ResponseErrorMap<>();

        Tenant tenant = Tenant.builder()
                .state(State.ENABLED)
                .build();

        fieldErrors.put("name", serviceHelper.updateNameField(tenant::setName, request.getName(), finalAcceptLanguage));
        fieldErrors.put("language", Validator.validateLanguage(request.getLanguage(), finalAcceptLanguage));

        if (fieldErrors.size() > 0)
            throw new BadFieldsResponse(HttpStatus.BAD_REQUEST, fieldErrors);


        tenant.setCreatedAt(Instant.now().getEpochSecond());
        tenant.setUpdatedAt(Instant.now().getEpochSecond());
        tenantRepository.save(tenant);

        {
            Language language1 = Language.builder().build();
            language1.setTenant(tenant);
            language1.setName("en-US");
            language1.setLabel("English");
            language1.setCreatedAt(Instant.now().getEpochSecond());
            language1.setUpdatedAt(Instant.now().getEpochSecond());
            languageRepository.save(language1);

            Language language2 = Language.builder().build();
            language2.setTenant(tenant);
            language2.setName("uk-UA");
            language2.setLabel("Українська");
            language2.setCreatedAt(Instant.now().getEpochSecond());
            language2.setUpdatedAt(Instant.now().getEpochSecond());
            languageRepository.save(language2);

            List<Language> languageList = languageRepository.findAllByTenant_TenantId(tenant.getTenantId());

            tenant.setLanguage(request.getLanguage());
            ObjectMapper objectMapper = new ObjectMapper();
            Content content = Content.builder().build();

            Map<String, String> languagesMap = content.getName();
            for (Language language : languageList) {
                languagesMap.put(language.getName(), null);
                System.out.println(language.getName());
            }
            languagesMap.put(tenant.getLanguage(), Validator.removeExtraSpaces(tenant.getName()));

            Translation translation = Translation.builder().build();
            translation.setReferenceId(tenant.getTenantId());
            translation.setReferenceType("tenant");
            translation.setContent(objectMapper.writeValueAsString(content));
            translation.setCreatedAt(Instant.now().getEpochSecond());
            translation.setUpdatedAt(Instant.now().getEpochSecond());
            translationRepository.save(translation);
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

        Tenant tenant = tenantRepository.findByTenantId(tenantId).orElseThrow(() -> {
            ExceptionTools.notFoundResponse(".tenantNotFound", finalAcceptLanguage, tenantId);
            return null;
        });

        Translation translationFromDB = translationRepository.findAllByTranslationId(tenant.getTranslationId()).orElseThrow(() -> {
            ExceptionTools.notFoundResponse(".translationNotFound", finalAcceptLanguage, tenantId);
            return null;
        });

        fieldErrors.put("name", requiredServiceHelper.updateNameIfNeeded(request.getName(), tenant, finalAcceptLanguage));
        fieldErrors.put("language", Validator.validateLanguage(request.getLanguage(), finalAcceptLanguage));

        if (fieldErrors.size() > 0)
            throw new BadFieldsResponse(HttpStatus.BAD_REQUEST, fieldErrors);

        ObjectMapper objectMapper = new ObjectMapper();
        Content content = objectMapper.readValue(translationFromDB.getContent(), Content.class);
        content.getName().put(request.getLanguage(), Validator.removeExtraSpaces(tenant.getName()));
        translationFromDB.setContent(objectMapper.writeValueAsString(content));

        tenant.setUpdatedAt(Instant.now().getEpochSecond());
        tenantRepository.save(tenant);
        loggingService.log(LogLevel.INFO, String.format("patchTenantById %s %s", tenantId, Message.UPDATE.getMessage()));
    }

    public void deleteTenantById(String tenantId, boolean forceDelete, String acceptLanguage) {
        String finalAcceptLanguage = CheckHeader.checkHeaderLanguage(acceptLanguage);

        var tenantInfo = tenantRepository.findByTenantId(tenantId);

        Tenant tenant = tenantInfo.orElseThrow(() -> {
            ExceptionTools.notFoundResponse(".tenantNotFound", finalAcceptLanguage, tenantId);
            return null;
        });

        if (!forceDelete) {
            tenant.setState(State.DISABLED);
            tenantRepository.save(tenant);
        } else {
            tenantRepository.delete(tenant);
        }

        loggingService.log(LogLevel.INFO, String.format("deleteTenantById %s NAME=%s %s", tenantId, tenant.getName(), Message.DELETE.getMessage()));
    }
}