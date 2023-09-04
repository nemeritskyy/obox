package ua.com.obox.dbschema.associateddata;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ua.com.obox.dbschema.tools.exception.ExceptionTools;
import ua.com.obox.dbschema.tools.exception.Message;
import ua.com.obox.dbschema.tools.logging.LogLevel;
import ua.com.obox.dbschema.tools.logging.LoggingService;
import ua.com.obox.dbschema.tools.translation.CheckHeader;

import java.util.*;

@Service
@RequiredArgsConstructor
public class RestaurantAssociatedDataService {
    private final RestaurantAssociatedDataRepository dataRepository;
    private final LoggingService loggingService;

    public RestaurantAssociatedDataResponse getAssociatedDataById(String associatedDataId, String acceptLanguage) {
        String finalAcceptLanguage = CheckHeader.checkHeaderLanguage(acceptLanguage);

        var data = dataRepository.findByAssociatedId(associatedDataId);

        RestaurantAssociatedData associatedData = data.orElseThrow(() -> {
            ExceptionTools.notFoundResponse(".associatedNotFound", finalAcceptLanguage, associatedDataId);
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

        loggingService.log(LogLevel.INFO, String.format("getAssociatedDataById %s", associatedDataId));
        return RestaurantAssociatedDataResponse.builder()
                .associatedId(associatedData.getAssociatedId())
                .restaurantId(associatedData.getRestaurantId())
                .languageCode(associatedData.getLanguageCode())
                .allergens(allergens)
                .tags(tags)
                .build();
    }

    public void deleteAssociatedDataByRestaurantId(String restaurantId, String acceptLanguage) {
        String finalAcceptLanguage = CheckHeader.checkHeaderLanguage(acceptLanguage);

        var dataInfo = dataRepository.findByRestaurantId(restaurantId);

        RestaurantAssociatedData associatedData = dataInfo.orElseThrow(() -> {
            ExceptionTools.notFoundResponse(".associatedNotFound", finalAcceptLanguage, restaurantId);
            return null;
        });

        dataRepository.delete(associatedData);
        loggingService.log(LogLevel.INFO, String.format("deleteAssociatedDataByRestaurantId UUID=%s RESTAURANT=%s LANGUAGE CODE=%s %s",
                associatedData.getAssociatedId(), restaurantId, associatedData.getLanguageCode(), Message.DELETE.getMessage()));
    }
}
