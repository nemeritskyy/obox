package ua.com.obox.dbschema.dish;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/dishes")
@RequiredArgsConstructor
@Tag(name = "Dishes")
public class DishController {
    private final DishService service;

    @GetMapping("/{dishId}")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success", content = @Content(mediaType = "application/json",
                    schema = @Schema(example =
                            "{\n" +
                                    "    \"dish_id\": \"762dbe8e-4cfc-40f0-af3f-02cfc67da709\",\n" +
                                    "    \"category_id\": \"decb23a3-b437-4c28-95df-a93aa59d90a7\",\n" +
                                    "    \"associated_id\": \"915e9be8-a36b-40d9-bf9b-aeedc180958e\",\n" +
                                    "    \"name\": \"Dorblue\",\n" +
                                    "    \"description\": \"string\",\n" +
                                    "    \"price\": 300.0,\n" +
                                    "    \"weight\": 250,\n" +
                                    "    \"calories\": 400,\n" +
                                    "    \"allergens\": [\n" +
                                    "        \"Celery\",\n" +
                                    "        \"Cereals containing gluten\",\n" +
                                    "        \"Clams\"\n" +
                                    "    ],\n" +
                                    "    \"tags\": [\n" +
                                    "        \"Spicy\",\n" +
                                    "        \"Vegan\"\n" +
                                    "    ],\n" +
                                    "    \"image_url\": \"https://img.obox.com.ua/915e9be8-a36b-40d9-bf9b-aeedc180958e/762dbe8e-4cfc-40f0-af3f-02cfc67da709.jpg\",\n" +
                                    "    \"state\": \"ENABLED\"\n" +
                                    "}"
                    ))),
            @ApiResponse(responseCode = "404", description = "Not found", content = @Content(mediaType = "application/json",
                    schema = @Schema(example =
                            "{\n" +
                                    "    \"timestamp\": \"2023-08-23T08:00:50.333+00:00\",\n" +
                                    "    \"status\": 404,\n" +
                                    "    \"error\": \"Not Found\",\n" +
                                    "    \"message\": \"Dish with id 762dbe8e-4cxc-40f0-af3f-02cfc67da709 not found\",\n" +
                                    "    \"path\": \"/dishes/762dbe8e-4cxc-40f0-af3f-02cfc67da709\"\n" +
                                    "}"
                    )))
    })
    public ResponseEntity<DishResponse> getDishById(@PathVariable String dishId) {
        DishResponse dishResponse = service.getDishById(dishId);

        return ResponseEntity.ok(dishResponse);
    }

    @PostMapping("/")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Success", content = @Content(mediaType = "application/json",
                    schema = @Schema(example =
                            "{\n" +
                                    "  \"dish_id\": \"762dbe8e-4cfc-40f0-af3f-02cfc67da709\"\n" +
                                    "}"
                    ))),
            @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(mediaType = "application/json",
                    schema = @Schema(example =
                            "{\n" +
                                    "    \"timestamp\": \"2023-08-23T08:06:10.306+00:00\",\n" +
                                    "    \"status\": 400,\n" +
                                    "    \"error\": \"Bad Request\",\n" +
                                    "    \"message\": \"400 BAD_REQUEST\",\n" +
                                    "    \"path\": \"/dishes/\",\n" +
                                    "    \"fields\": {\n" +
                                    "        \"category_id\": \"Category with id null not found\",\n" +
                                    "        \"price\": \"Price cannot be empty\",\n" +
                                    "        \"name\": \"Field name is required\",\n" +
                                    "        \"state\": \"The state is incorrect\"\n" +
                                    "    }\n" +
                                    "}"
                    )))
    })
    public ResponseEntity<DishResponseId> createDish(@RequestBody
                                                     @Schema(example = "{\n" +
                                                             "  \"category_id\": \"uuid\",\n" +
                                                             "  \"name\": \"string\",\n" +
                                                             "  \"description\": \"string\",\n" +
                                                             "  \"price\": 1.99,\n" +
                                                             "  \"weight\": 0,\n" +
                                                             "  \"calories\": 0,\n" +
                                                             "    \"allergens\": [\n" +
                                                             "        \"Celery\",\n" +
                                                             "        \"Cereals containing gluten\",\n" +
                                                             "        \"Clams\"\n" +
                                                             "    ],\n" +
                                                             "    \"tags\": [\n" +
                                                             "        \"Spicy\",\n" +
                                                             "        \"Vegan\"\n" +
                                                             "    ],\n" +
                                                             "  \"state\": \"ENABLED or DISABLED\",\n" +
                                                             "  \"image\": \"Base64 only JPG and PNG (not necessary)\"\n" +
                                                             "}")
                                                     Dish request) {
        DishResponseId response = service.createDish(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("{dishId}")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "No Content"),
            @ApiResponse(responseCode = "404", description = "Not found", content = @Content(mediaType = "application/json",
                    schema = @Schema(example =
                            "{\n" +
                                    "    \"timestamp\": \"2023-08-23T08:12:06.421+00:00\",\n" +
                                    "    \"status\": 404,\n" +
                                    "    \"error\": \"Not Found\",\n" +
                                    "    \"message\": \"Dish with id cb18dfad-24bx-4a1c-aa84-c48bf356a3b6 not found\",\n" +
                                    "    \"path\": \"/dishes/cb18dfad-24bx-4a1c-aa84-c48bf356a3b6\"\n" +
                                    "}"
                    )))
    })
    public ResponseEntity<Void> patchDishById(@PathVariable String dishId, @RequestBody
    @Schema(example = "{\n" +
            "  \"category_id\": \"uuid\",\n" +
            "  \"name\": \"string\",\n" +
            "  \"description\": \"string\",\n" +
            "  \"price\": 1.99,\n" +
            "  \"weight\": 0,\n" +
            "  \"calories\": 0,\n" +
            "    \"allergens\": [\n" +
            "        \"Celery\",\n" +
            "        \"Cereals containing gluten\",\n" +
            "        \"Clams\"\n" +
            "    ],\n" +
            "    \"tags\": [\n" +
            "        \"Spicy\",\n" +
            "        \"Vegan\"\n" +
            "    ],\n" +
            "  \"state\": \"ENABLED or DISABLED\",\n" +
            "  \"image\": \"Base64 only JPG and PNG (not necessary)\"\n" +
            "}")
    Dish request) {
        service.patchDishById(dishId, request);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{dishId}")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "No Content"),
            @ApiResponse(responseCode = "404", description = "Not found", content = @Content(mediaType = "application/json",
                    schema = @Schema(example =
                            "{\n" +
                                    "    \"timestamp\": \"2023-08-23T08:13:36.104+00:00\",\n" +
                                    "    \"status\": 404,\n" +
                                    "    \"error\": \"Not Found\",\n" +
                                    "    \"message\": \"Dish with id c8e7375d-1dbf-4d40-ae65-x1cd1e6e973f not found\",\n" +
                                    "    \"path\": \"/dishes/c8e7375d-1dbf-4d40-ae65-x1cd1e6e973f\"\n" +
                                    "}"
                    )))
    })
    public ResponseEntity<Void> deleteDishById(@PathVariable String dishId) {
        service.deleteDishById(dishId);
        return ResponseEntity.noContent().build();
    }
}

