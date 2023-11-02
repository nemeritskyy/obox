package ua.com.obox.dbschema.associateddata;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static ua.com.obox.dbschema.tools.examples.RestaurantAssociatedDataResponseExample.*;

@RestController
@RequestMapping("/associated")
@RequiredArgsConstructor
@Tag(name = "AssociatedData")
public class RestaurantAssociatedDataController {
    private final RestaurantAssociatedDataService associatedDataService;

    @GetMapping("/{associatedDataId}")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success", content = @Content(mediaType = "application/json",
                    schema = @Schema(example = GET_200_RESPONSE_EXAMPLE))),
            @ApiResponse(responseCode = "404", description = "Not found", content = @Content(mediaType = "application/json",
                    schema = @Schema(example = ALL_MAPPINGS_404_RESPONSE_EXAMPLE)))
    })
    public ResponseEntity<RestaurantAssociatedDataResponse> getAssociatedDataById(@PathVariable String associatedDataId, @RequestHeader HttpHeaders httpHeaders) {
        String acceptLanguage = httpHeaders.getFirst("Accept-Language");
        RestaurantAssociatedDataResponse associatedDataResponse = associatedDataService.getAssociatedDataById(associatedDataId, acceptLanguage);
        return ResponseEntity.ok(associatedDataResponse);
    }

    @DeleteMapping("/{restaurantId}")
    @Hidden
    public ResponseEntity<Void> deleteAssociatedDataByRestaurantId(@PathVariable String restaurantId, @RequestHeader HttpHeaders httpHeaders) {
        String acceptLanguage = httpHeaders.getFirst("Accept-Language");
        associatedDataService.deleteAssociatedDataByRestaurantId(restaurantId, acceptLanguage);
        return ResponseEntity.noContent().build();
    }
}