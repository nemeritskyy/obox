package ua.com.obox.dbschema.sorting;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/order/")
@RequiredArgsConstructor
@Tag(name = "Entity Order (Sorting)")
public class EntityOrderController {
    private final EntityOrderService service;

    @PostMapping("/")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "No Content"),
            @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(mediaType = "application/json",
                    schema = @Schema(example = "{\n" +
                            "    \"timestamp\": \"2023-10-13T13:13:40.233+00:00\",\n" +
                            "    \"status\": 400,\n" +
                            "    \"error\": \"Bad Request\",\n" +
                            "    \"message\": \"400 BAD_REQUEST\",\n" +
                            "    \"path\": \"/order/\",\n" +
                            "    \"fields\": {\n" +
                            "        \"reference_type\": \"Bad reference type\"\n" +
                            "    }\n" +
                            "}"))),
            @ApiResponse(responseCode = "404", description = "Not found", content = @Content(mediaType = "application/json",
                    schema = @Schema(example = "{\n" +
                            "    \"timestamp\": \"2023-10-13T13:02:31.586+00:00\",\n" +
                            "    \"status\": 404,\n" +
                            "    \"error\": \"Not Found\",\n" +
                            "    \"message\": \"When trying to refresh the sorted list, the entity was not found\",\n" +
                            "    \"path\": \"/order/\"\n" +
                            "}")))
    })
    public ResponseEntity<Void> createMenu(@RequestBody @Schema(example = "{\n" +
            "    \"entity_id\":\"f08de446-7622-4c3-b5bd-a4da773ca3274\",\n" +
            "    \"reference_type\":\"CATEGORY\",\n" +
            "    \"sorted_list\": [\n" +
            "        \"2cbd12a3-9f7d-458b-8509-91bf6cc5b190\",\n" +
            "        \"544f8740-9574-4be7-8633-38d29b508554\",\n" +
            "        \"1f1679bd-7c0e-4645-b70e-83473f70da81\"\n" +
            "    ]\n" +
            "}") EntityOrder request, @RequestHeader HttpHeaders httpHeaders) {
        String acceptLanguage = httpHeaders.getFirst("Accept-Language");
        service.createEntityOrder(request, acceptLanguage);
        return ResponseEntity.noContent().build();
    }
}
