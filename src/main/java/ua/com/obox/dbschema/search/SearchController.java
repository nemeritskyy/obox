package ua.com.obox.dbschema.search;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ua.com.obox.dbschema.category.CategoryResponse;

import java.util.List;

import static ua.com.obox.dbschema.tools.examples.SearchResponseExample.*;


@RestController
@RequestMapping("/search/")
@RequiredArgsConstructor
@Tag(name = "Search")
public class SearchController {
    private final SearchService service;
    @GetMapping("/{allergenId}/allergens")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success", content = @Content(mediaType = "application/json",
                    schema = @Schema(example = GET_ALLERGENS_200_RESPONSE_EXAMPLE))),
            @ApiResponse(responseCode = "404", description = "Not found", content = @Content(mediaType = "application/json",
                    schema = @Schema(example = ALLERGEN_404_RESPONSE_EXAMPLE)))
    })
    public ResponseEntity<List<CategoryResponse>> getAllDishesByAllergenId(@PathVariable String allergenId, @RequestHeader HttpHeaders httpHeaders) {
        String acceptLanguage = httpHeaders.getFirst("Accept-Language");
        List<CategoryResponse> categoryResponseList = service.getAllDishesByAllergenId(allergenId, acceptLanguage);
        return ResponseEntity.ok(categoryResponseList);
    }

    @GetMapping("/{markId}/marks")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success", content = @Content(mediaType = "application/json",
                    schema = @Schema(example = GET_MARKS_200_RESPONSE_EXAMPLE))),
            @ApiResponse(responseCode = "404", description = "Not found", content = @Content(mediaType = "application/json",
                    schema = @Schema(example = MARK_404_RESPONSE_EXAMPLE)))
    })
    public ResponseEntity<List<CategoryResponse>> getAllMarksByAllergenId(@PathVariable String markId, @RequestHeader HttpHeaders httpHeaders) {
        String acceptLanguage = httpHeaders.getFirst("Accept-Language");
        List<CategoryResponse> categoryResponseList = service.getAllMarksByAllergenId(markId, acceptLanguage);
        return ResponseEntity.ok(categoryResponseList);
    }

    @PostMapping
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success", content = @Content(mediaType = "application/json",
                    schema = @Schema(example = GET_QUERY_RESULTS))),
    })
    public ResponseEntity<List<SearchResponse>> searchAllByQuery(@RequestBody @Schema(example = POST_QUERY_EXAMPLE) SearchQuery query, @RequestHeader HttpHeaders httpHeaders) throws JsonProcessingException {
        String acceptLanguage = httpHeaders.getFirst("Accept-Language");
        List<SearchResponse> dishesResponse = service.searchAllByQuery(query, acceptLanguage);
        return ResponseEntity.ok(dishesResponse);
    }
}
