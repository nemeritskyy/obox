package tools.values;

import org.apache.commons.lang3.RandomStringUtils;

import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.*;

public class TestValuesDishes {
    private static Stream<Map<String, Object>> getValidPostRequests() {
        return Stream.of(
                Map.of( // Post. 1
                        "category_id", SupportingData.categoryId,
                        "name", "dish1",
                        "description", "description for dish 1",
                        "price", 1,
                        "weight", 1,
                        "calories", 1,
                        "in_stock", "ENABLED",
                        "state", "ENABLED",
                        "language", "en-US"
                ),
                Map.of( // Post. 3
                        "category_id", SupportingData.categoryId,
                        "name", RandomStringUtils.random(200, true, true),
                        "description", "description for dish 1",
                        "price", 1,
                        "weight", 1,
                        "calories", 1,
                        "in_stock", "ENABLED",
                        "state", "ENABLED",
                        "language", "en-US"
                ),
                Map.of( // Post. 11
                        "category_id", SupportingData.categoryId,
                        "name", "dish3",
                        "description", "description for dish 1",
                        "price", 100_000,
                        "weight", 1,
                        "calories", 1,
                        "in_stock", "ENABLED",
                        "state", "ENABLED",
                        "language", "en-US"
                ),
                Map.of( // Post. 14
                        "category_id", SupportingData.categoryId,
                        "name", "dish4",
                        "description", "description for dish 1",
                        "price", 99.99,
                        "weight", 1,
                        "calories", 1,
                        "in_stock", "ENABLED",
                        "state", "ENABLED",
                        "language", "en-US"
                ),
                Map.of( // Post. 19
                        "category_id", SupportingData.categoryId,
                        "name", "dish5",
                        "description", "description for dish 1",
                        "price", 1,
                        "weight", 1,
                        "calories", 1,
                        "in_stock", "ENABLED",
                        "state", "DISABLED",
                        "language", "en-US"
                ),
                Map.of( // Post. 20
                        "category_id", SupportingData.categoryId,
                        "name", "dish6",
                        "description", RandomStringUtils.random(255, true, true),
                        "price", 1,
                        "weight", 1,
                        "calories", 1,
                        "in_stock", "ENABLED",
                        "state", "ENABLED",
                        "language", "en-US"
                ),
                Map.of( // Post. 22
                        "category_id", SupportingData.categoryId,
                        "name", "dish7",
                        "description", RandomStringUtils.random(255, true, true),
                        "price", 1,
                        "weight", 10_000,
                        "calories", 1,
                        "in_stock", "ENABLED",
                        "state", "ENABLED",
                        "language", "en-US"
                ),
                Map.of( // Post. 22
                        "category_id", SupportingData.categoryId,
                        "name", "dish weight",
                        "description", RandomStringUtils.random(255, true, true),
                        "price", 1,
                        "weight", "10000/300/50",
                        "calories", 1,
                        "in_stock", "ENABLED",
                        "state", "ENABLED",
                        "language", "en-US"
                ),
                Map.of( // Post. 26
                        "category_id", SupportingData.categoryId,
                        "name", "dish8",
                        "description", "description",
                        "price", 1,
                        "weight", 100/50,
                        "calories", 30_000,
                        "in_stock", "ENABLED",
                        "state", "ENABLED",
                        "language", "en-US"
                )
        );
    }

    private static Stream<Map<String, Object>> getInvalidPostRequests() {
        return Stream.of(
                Map.of( // Post. 2
                        "category_id", SupportingData.categoryId,
                        "name", "",
                        "description", "description for dish 1",
                        "price", 1,
                        "weight", 1,
                        "calories", 1,
                        "state", "ENABLED"
                ),
                Map.of( // Post. 4
                        "category_id", SupportingData.categoryId,
                        "name", RandomStringUtils.random(201, true, true),
                        "description", "description",
                        "price", 1,
                        "weight", 1,
                        "calories", 1,
                        "state", "ENABLED"
                ),
                Map.of( // Post. 6
                        "category_id", SupportingData.categoryId,
                        "name", " ",
                        "description", "description",
                        "price", 1,
                        "weight", 1,
                        "calories", 1,
                        "state", "ENABLED"
                ),
                Map.of( // Post. 6 duplicate number // Post. 7
                        "category_id", SupportingData.categoryId,
                        "name", "dish1",
                        "description", "description",
                        "price", "",
                        "weight", 1,
                        "calories", 1,
                        "state", "ENABLED"
                ),
                Map.of( // Post. 7
                        "category_id", SupportingData.categoryId,
                        "name", "dish1",
                        "description", "description",
                        "price", "",
                        "weight", 1,
                        "calories", 1,
                        "state", "ENABLED"
                ),
                Map.of( // Post. 8
                        "category_id", SupportingData.categoryId,
                        "name", " ",
                        "description", "description",
                        "price", "aaa",
                        "weight", 1,
                        "calories", 1,
                        "state", "ENABLED"
                ),
                Map.of( // Post. 9
                        "category_id", SupportingData.categoryId,
                        "name", "dish1",
                        "description", "description",
                        "price", ",",
                        "weight", 1,
                        "calories", 1,
                        "state", "ENABLED"
                ),
                Map.of( // Post. 10
                        "category_id", SupportingData.categoryId,
                        "name", "dish1",
                        "description", "description",
                        "price", -100,
                        "weight", 1,
                        "calories", 1,
                        "state", "ENABLED"
                ),
                Map.of( // Post. 12
                        "category_id", SupportingData.categoryId,
                        "name", "dish1",
                        "description", "description",
                        "price", 100_001,
                        "weight", 1,
                        "calories", 1,
                        "state", "ENABLED"
                ),
                Map.of( // Post. 13
                        "category_id", SupportingData.categoryId,
                        "name", "dish1",
                        "description", "description",
                        "price", "99,99",
                        "weight", 1,
                        "calories", 1,
                        "state", "ENABLED"
                ),
                Map.of( // Post. 15
                        "category_id", SupportingData.categoryId,
                        "name", "dish1",
                        "description", "",
                        "price", 1,
                        "weight", 1,
                        "calories", 1,
                        "state", "ENABLED"
                ),
                Map.of( // Post. 17
                        "category_id", SupportingData.categoryId,
                        "name", "dish1",
                        "description", "description",
                        "price", 1,
                        "weight", 1,
                        "calories", 1,
                        "state", ""
                ),
                Map.of( // Post. 18
                        "category_id", SupportingData.categoryId,
                        "name", "dish1",
                        "description", "description",
                        "price", 1,
                        "weight", 1,
                        "calories", 1,
                        "state", "true"
                ),
                Map.of( // Post. 21
                        "category_id", SupportingData.categoryId,
                        "name", "dish1",
                        "description", RandomStringUtils.random(256, true, true),
                        "price", 1,
                        "weight", 1,
                        "calories", 1,
                        "state", "ENABLED"
                ),
                Map.of( // Post. 23
                        "category_id", SupportingData.categoryId,
                        "name", "dish1",
                        "description", RandomStringUtils.random(255, true, true),
                        "price", 1,
                        "weight", 100_001,
                        "calories", 1,
                        "state", "ENABLED"
                ),
                Map.of( // Post. 25
                        "category_id", SupportingData.categoryId,
                        "name", "dish1",
                        "description", "description",
                        "price", 1,
                        "weight", 100.5,
                        "calories", 30_001,
                        "state", "ENABLED"
                )
        );
    }

    private static Stream<Map<String, Object>> getValidPatchRequests() {
        return Stream.of(
                Map.of( // Patch. 1
                        "name", "dish21",
                        "description", "description",
                        "price", 1,
                        "weight", 10000,
                        "calories", 100.5,
                        "state", "ENABLED",
                        "language", "en-US"
                ),
                Map.of( // Patch. 2
                        "name", "dish22",
                        "price", 100,
                        "description", "new description",
                        "weight", 10000,
                        "calories", 100.5,
                        "state", "ENABLED",
                        "language", "en-US"
                ),
                Map.of( // Patch. 3
                        "name", "dish23",
                        "price", 8,
                        "description", "new description",
                        "calories", 100.5,
                        "weight", 10000,
                        "state", "ENABLED",
                        "language", "en-US"
                ),
                Map.of( // Patch. 3 duplicate
                        "name", "dish24",
                        "price", 8,
                        "description", "",
                        "calories", 100.5,
                        "weight", 10000,
                        "state", "ENABLED",
                        "language", "en-US"
                ),
                Map.of( // Patch. 4
                        "calories", 500,
                        "weight", 400,
                        "language", "en-US"
                ),
                Map.of( // Patch. 6
                        "state", "DISABLED",
                        "language", "en-US"
                ),
                Map.of( // change for Get. 5
                        "description", "",
                        "calories", "",
//                        "allergens", "",
//                        "tags", "",
                        "weight", "",
                        "language", "en-US"
                )
        );
    }

    private static Stream<Map<String, Object>> getInvalidPatchRequests() {
        return Stream.of(
                Map.of( // Patch. 5
                        "weight", 100_001,
                        "calories", 30_001,
                        "price", 100_001
                ),
                Map.of( // Patch. 7
                        "state", "true"
                )
        );
    }

    private static Stream<Map<String, Object>> getGetValidRequests() {
        return Stream.of(
                Map.of( // Get. 1
                        "$.state", equalTo("DISABLED")
                ),
                Map.of( // Test category data
                        "$.category_id", notNullValue()
                ),
                Map.of( // Get. 5
                        "$.description", "DOES_NOT_EXIST",
                        "$.calories", "DOES_NOT_EXIST",
//                        "$.allergens", "DOES_NOT_EXIST",
//                        "$.tags", "DOES_NOT_EXIST",
                        "$.weight", "DOES_NOT_EXIST"
                )
        );
    }

    public static Stream<String> getNonExistingId() {
        return Stream.of(
                UUID.randomUUID().toString(), // Get. 3
                SupportingData.dishId // Get. 2
        );
    }

}
