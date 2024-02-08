package ua.com.obox.dbschema.attachment;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

import static ua.com.obox.dbschema.tools.examples.AttachmentsResponseExample.*;

@RestController
@RequestMapping("/attachments")
@PreAuthorize("hasAnyRole('ADMIN','USER')")
@RequiredArgsConstructor

@Tag(name = "Attachments")
public class AttachmentController {
    private final AttachmentService service;

    @Hidden
    @GetMapping("/{entityId}/attachments")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success", content = @Content(mediaType = "application/json",
                    schema = @Schema(example = GET_ALL_200_RESPONSE_EXAMPLE)))
    })
    public ResponseEntity<List<AttachmentResponse>> getAllAttachmentsByEntityId(@PathVariable String entityId, @RequestHeader HttpHeaders httpHeaders) {
        String acceptLanguage = httpHeaders.getFirst("Accept-Language");
        List<AttachmentResponse> categoryResponses = service.getAllAttachmentsByEntityId(entityId, acceptLanguage);
        return ResponseEntity.ok(categoryResponses);
    }

    @PostMapping("/")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Success", content = @Content(mediaType = "application/json",
                    schema = @Schema(example = POST_201_RESPONSE_EXAMPLE))),
            @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(mediaType = "application/json",
                    schema = @Schema(example = POST_400_RESPONSE_EXAMPLE)))
    })
    public ResponseEntity<AttachmentResponseId> createAttachment(@RequestBody @Schema(example = POST_BODY) Attachment request,
                                                                 @RequestHeader HttpHeaders httpHeaders) throws IOException {
        String acceptLanguage = httpHeaders.getFirst("Accept-Language");
        AttachmentResponseId response = service.createAttachment(request, acceptLanguage);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{attachmentId}")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success", content = @Content(mediaType = "application/json",
                    schema = @Schema(example = GET_200_RESPONSE_EXAMPLE))),
            @ApiResponse(responseCode = "404", description = "Not found", content = @Content(mediaType = "application/json",
                    schema = @Schema(example = ALL_MAPPINGS_404_RESPONSE_EXAMPLE)))
    })
    public ResponseEntity<AttachmentResponse> getAttachmentById(@PathVariable String attachmentId, @RequestHeader HttpHeaders httpHeaders) {
        String acceptLanguage = httpHeaders.getFirst("Accept-Language");
        AttachmentResponse response = service.getAttachmentById(attachmentId, acceptLanguage);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{attachmentId}")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "No Content"),
            @ApiResponse(responseCode = "404", description = "Not found", content = @Content(mediaType = "application/json",
                    schema = @Schema(example = ALL_MAPPINGS_404_RESPONSE_EXAMPLE)))
    })
    public ResponseEntity<Void> deleteAttachmentById(@PathVariable String attachmentId, @RequestHeader HttpHeaders httpHeaders) {
        String acceptLanguage = httpHeaders.getFirst("Accept-Language");
        service.deleteAttachmentById(attachmentId, acceptLanguage);
        return ResponseEntity.noContent().build();
    }
}