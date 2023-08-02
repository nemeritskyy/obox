package ua.com.obox.dbschema.associateddata;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import ua.com.obox.dbschema.tools.exception.ExceptionTools;
import ua.com.obox.dbschema.tools.exception.Message;
import ua.com.obox.dbschema.tools.logging.LogLevel;
import ua.com.obox.dbschema.tools.logging.LoggingService;

@Service
@RequiredArgsConstructor
public class RestaurantAssociatedDataService {
    private final RestaurantAssociatedDataRepository dataRepository;
    private final LoggingService loggingService;
    private String loggingMessage;
    public RestaurantAssociatedDataResponse getAssociatedDataById(String associatedDataId) {
        loggingMessage = ExceptionTools.generateLoggingMessage("getAssociatedDataById", associatedDataId);
        var data = dataRepository.findByAssociatedId(associatedDataId);
        RestaurantAssociatedData associatedData = data.orElseThrow(() -> {
            loggingService.log(LogLevel.ERROR, loggingMessage + Message.NOT_FOUND.getMessage());
            return new ResponseStatusException(HttpStatus.NOT_FOUND, "Associated data with id " + associatedDataId + Message.NOT_FOUND.getMessage());
        });
        loggingService.log(LogLevel.INFO, loggingMessage);
        return RestaurantAssociatedDataResponse.builder()
                .associatedId(associatedData.getAssociatedId())
                .restaurantId(associatedData.getRestaurantId())
                .languageCode(associatedData.getLanguageCode())
                .allergens(associatedData.getAllergens())
                .tags(associatedData.getTags())
                .build();
    }

}
