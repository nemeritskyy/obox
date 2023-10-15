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

import static ua.com.obox.dbschema.tools.examples.OrderResponseExample.*;

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
                    schema = @Schema(example = POST_400_RESPONSE_EXAMPLE))),
            @ApiResponse(responseCode = "404", description = "Not found", content = @Content(mediaType = "application/json",
                    schema = @Schema(example = POST_404_RESPONSE_EXAMPLE)))
    })
    public ResponseEntity<Void> createMenu(@RequestBody @Schema(example = POST_REQUEST_BODY_EXAMPLE) EntityOrder request, @RequestHeader HttpHeaders httpHeaders) {
        String acceptLanguage = httpHeaders.getFirst("Accept-Language");
        service.createEntityOrder(request, acceptLanguage);
        return ResponseEntity.noContent().build();
    }
}
