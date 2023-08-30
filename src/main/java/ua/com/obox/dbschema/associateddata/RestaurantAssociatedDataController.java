package ua.com.obox.dbschema.associateddata;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/associated")
@RequiredArgsConstructor
@Tag(name = "AssociatedData")
public class RestaurantAssociatedDataController {
    private final RestaurantAssociatedDataService associatedDataService;

    @GetMapping("/{associatedDataId}")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success", content = @Content(mediaType = "application/json",
                    schema = @Schema(example =
                            """
                                    {
                                        "associated_id": "b7830129-53f1-4e1b-8a55-398462697ac0",
                                        "restaurant_id": "9aff3e00-451c-490e-b48b-c4315785b75e",
                                        "language_code": "en",
                                        "allergens": [
                                            "Celery",
                                            "Cereals containing gluten",
                                            "Crustaceans",
                                            "Eggs",
                                            "Fish",
                                            "Honey",
                                            "Lupin",
                                            "Milk",
                                            "Molluscs",
                                            "Mustard",
                                            "Nuts",
                                            "Peanuts",
                                            "Sesame seeds",
                                            "Soybeans",
                                            "Sulphur dioxide and sulphites"
                                        ],
                                        "tags": [
                                            "Gluten-free",
                                            "Medium-spicy",
                                            "Recommended",
                                            "Spicy",
                                            "Vegan",
                                            "Vegetarian"
                                        ]
                                    }"""
                    ))),
            @ApiResponse(responseCode = "404", description = "Not found", content = @Content(mediaType = "application/json",
                    schema = @Schema(example =
                            """
                                    {
                                        "timestamp": "2023-08-29T02:52:11.105+00:00",
                                        "status": 404,
                                        "error": "Not Found",
                                        "message": "Associated data with id b7830129-53f1-4e1b-xa55-398462697ac0 not found",
                                        "path": "/associated/b7830129-53f1-4e1b-xa55-398462697ac0"
                                    }""")))
    })
    public ResponseEntity<RestaurantAssociatedDataResponse> getAssociatedDataById(@PathVariable String associatedDataId) {
        RestaurantAssociatedDataResponse associatedDataResponse = associatedDataService.getAssociatedDataById(associatedDataId);
        return ResponseEntity.ok(associatedDataResponse);
    }

    @DeleteMapping("/{restaurantId}")
    @Hidden
    public ResponseEntity<Void> deleteAssociatedDataByRestaurantId(@PathVariable String restaurantId) {
        associatedDataService.deleteAssociatedDataByRestaurantId(restaurantId);
        return ResponseEntity.noContent().build();
    }

}
