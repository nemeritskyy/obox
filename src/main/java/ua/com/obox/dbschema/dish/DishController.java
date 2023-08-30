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
                            """
                                    {
                                        "dish_id": "762dbe8e-4cfc-40f0-af3f-02cfc67da709",
                                        "category_id": "decb23a3-b437-4c28-95df-a93aa59d90a7",
                                        "associated_id": "915e9be8-a36b-40d9-bf9b-aeedc180958e",
                                        "name": "Dorblue",
                                        "description": "string",
                                        "price": 300.0,
                                        "weight": 250,
                                        "calories": 400,
                                        "allergens": [
                                            "Celery",
                                            "Cereals containing gluten",
                                            "Clams"
                                        ],
                                        "tags": [
                                            "Spicy",
                                            "Vegan"
                                        ],
                                        "image_url": "https://img.obox.com.ua/915e9be8-a36b-40d9-bf9b-aeedc180958e/762dbe8e-4cfc-40f0-af3f-02cfc67da709.jpg",
                                        "state": "ENABLED"
                                    }"""
                    ))),
            @ApiResponse(responseCode = "404", description = "Not found", content = @Content(mediaType = "application/json",
                    schema = @Schema(example =
                            """
                                    {
                                        "timestamp": "2023-08-23T08:00:50.333+00:00",
                                        "status": 404,
                                        "error": "Not Found",
                                        "message": "Dish with id 762dbe8e-4cxc-40f0-af3f-02cfc67da709 not found",
                                        "path": "/dishes/762dbe8e-4cxc-40f0-af3f-02cfc67da709"
                                    }"""
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
                            """
                                    {
                                      "dish_id": "762dbe8e-4cfc-40f0-af3f-02cfc67da709"
                                    }"""
                    ))),
            @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(mediaType = "application/json",
                    schema = @Schema(example =
                            """
                                    {
                                        "timestamp": "2023-08-23T08:06:10.306+00:00",
                                        "status": 400,
                                        "error": "Bad Request",
                                        "message": "400 BAD_REQUEST",
                                        "path": "/dishes/",
                                        "fields": {
                                            "category_id": "Category with id null not found",
                                            "price": "Price cannot be empty",
                                            "name": "Field name is required",
                                            "state": "The state is incorrect"
                                        }
                                    }"""
                    )))
    })
    public ResponseEntity<DishResponseId> createDish(@RequestBody
                                                     @Schema(example = """
                                                             {
                                                               "category_id": "uuid",
                                                               "name": "string",
                                                               "description": "string",
                                                               "price": 1.99,
                                                               "weight": 0,
                                                               "calories": 0,
                                                                 "allergens": [
                                                                     "Celery",
                                                                     "Cereals containing gluten",
                                                                     "Clams"
                                                                 ],
                                                                 "tags": [
                                                                     "Spicy",
                                                                     "Vegan"
                                                                 ],
                                                               "state": "ENABLED or DISABLED",
                                                               "images": "Base64 only JPG and PNG (not necessary)"
                                                             }""")
                                                     Dish request) {
        DishResponseId response = service.createDish(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("{dishId}")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "No Content"),
            @ApiResponse(responseCode = "404", description = "Not found", content = @Content(mediaType = "application/json",
                    schema = @Schema(example =
                            """
                                    {
                                        "timestamp": "2023-08-23T08:12:06.421+00:00",
                                        "status": 404,
                                        "error": "Not Found",
                                        "message": "Dish with id cb18dfad-24bx-4a1c-aa84-c48bf356a3b6 not found",
                                        "path": "/dishes/cb18dfad-24bx-4a1c-aa84-c48bf356a3b6"
                                    }"""
                    )))
    })
    public ResponseEntity<Void> patchDishById(@PathVariable String dishId, @RequestBody
    @Schema(example = """
            {
              "category_id": "uuid",
              "name": "string",
              "description": "string",
              "price": 1.99,
              "weight": 0,
              "calories": 0,
                "allergens": [
                    "Celery",
                    "Cereals containing gluten",
                    "Clams"
                ],
                "tags": [
                    "Spicy",
                    "Vegan"
                ],
              "state": "ENABLED or DISABLED",
              "images": "Base64 only JPG and PNG (not necessary)"
            }""")
    Dish request) {
        service.patchDishById(dishId, request);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{dishId}")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "No Content"),
            @ApiResponse(responseCode = "404", description = "Not found", content = @Content(mediaType = "application/json",
                    schema = @Schema(example =
                            """
                                    {
                                        "timestamp": "2023-08-23T08:13:36.104+00:00",
                                        "status": 404,
                                        "error": "Not Found",
                                        "message": "Dish with id c8e7375d-1dbf-4d40-ae65-x1cd1e6e973f not found",
                                        "path": "/dishes/c8e7375d-1dbf-4d40-ae65-x1cd1e6e973f"
                                    }"""
                    )))
    })
    public ResponseEntity<Void> deleteDishById(@PathVariable String dishId) {
        service.deleteDishById(dishId);
        return ResponseEntity.noContent().build();
    }
}

