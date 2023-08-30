package ua.com.obox.dbschema.associateddata;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import ua.com.obox.dbschema.tools.exception.Message;
import ua.com.obox.dbschema.tools.logging.LogLevel;
import ua.com.obox.dbschema.tools.logging.LoggingService;
import ua.com.obox.dbschema.tools.services.AbstractResponseService;
import ua.com.obox.dbschema.tools.services.LoggingResponseHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RestaurantAssociatedDataService extends AbstractResponseService {
    private final RestaurantAssociatedDataRepository dataRepository;
    private final LoggingService loggingService;
    private String loggingMessage;
    private String responseMessage;

    public RestaurantAssociatedDataResponse getAssociatedDataById(String associatedDataId) {
        RestaurantAssociatedData associatedData;
        loggingMessage = "getAssociatedDataById";
        responseMessage = String.format("Associated data with id %s", associatedDataId);
        var data = dataRepository.findByAssociatedId(associatedDataId);

        associatedData = data.orElseThrow(() -> {
            notFoundResponse(associatedDataId);
            return null;
        });

        List<String> allergens = new ArrayList<>();
        if (associatedData.getAllergens() != null)
            allergens.addAll(Arrays.stream(associatedData.getAllergens().split("::")).toList());
        Collections.sort(allergens);

        List<String> tags = new ArrayList<>();
        if (associatedData.getTags() != null)
            tags.addAll(Arrays.stream(associatedData.getTags().split("::")).toList());
        Collections.sort(tags);

        loggingService.log(LogLevel.INFO, String.format("%s %s", loggingMessage, associatedDataId));
        return RestaurantAssociatedDataResponse.builder()
                .associatedId(associatedData.getAssociatedId())
                .restaurantId(associatedData.getRestaurantId())
                .languageCode(associatedData.getLanguageCode())
                .allergens(allergens)
                .tags(tags)
                .build();
    }

    public void deleteAssociatedDataByRestaurantId(String restaurantId) {
        RestaurantAssociatedData associatedData;
        loggingMessage = "deleteAssociatedDataByRestaurantId";
        responseMessage = String.format("Associated data with restaurant id %s", restaurantId);
        var dataInfo = dataRepository.findByRestaurantId(restaurantId);

        associatedData = dataInfo.orElseThrow(() -> {
            notFoundResponse(restaurantId);
            return null;
        });

        dataRepository.delete(associatedData);
        loggingService.log(LogLevel.INFO, String.format("%s UUID=%s RESTAURANT=%s LANGUAGE CODE=%s %s",
                loggingMessage, associatedData.getAssociatedId(), restaurantId, associatedData.getLanguageCode(), Message.DELETE.getMessage()));
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
