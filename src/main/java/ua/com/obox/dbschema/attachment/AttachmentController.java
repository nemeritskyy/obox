package ua.com.obox.dbschema.attachment;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/attachments")
@RequiredArgsConstructor

@Tag(name = "Attachments")
public class AttachmentController {
    private final AttachmentService service;

    @GetMapping("/{entityId}/attachments")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success", content = @Content(mediaType = "application/json",
                    schema = @Schema(example =
                            """
                                    [
                                        {
                                            "attachment_id": "5270e95c-a399-4c91-b3fe-725e0061c7bb",
                                            "reference_id": "0b4b702a-fff6-4682-bb2a-9207c50c7cab",
                                            "reference_type": "DISH",
                                            "attachment_url": "https://attachments.obox.pp.ua/b7830129-53f1-4e1b-8a55-398462697ac0/30804971-8cb6-4fd2-ad35-7c3f26ca2286.jpg"
                                        },
                                        {
                                            "attachment_id": "4a464ce2-2452-4850-a879-5830b8faa7ad",
                                            "reference_id": "0b4b702a-fff6-4682-bb2a-9207c50c7cab",
                                            "reference_type": "DISH",
                                            "attachment_url": "https://attachments.obox.pp.ua/b7830129-53f1-4e1b-8a55-398462697ac0/004c8205-4308-422e-8dd8-f104034aa4de.jpg"
                                        }
                                    ]
                                    """

                    )))
    })
    public ResponseEntity<List<AttachmentResponse>> getAllAttachmentsByEntityId(@PathVariable String entityId, @RequestHeader HttpHeaders httpHeaders) {
        String acceptLanguage = httpHeaders.getFirst("Accept-Language");
        List<AttachmentResponse> categoryResponses = service.getAllAttachmentsByEntityId(entityId, acceptLanguage);
        return ResponseEntity.ok(categoryResponses);
    }

    @PostMapping("/")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Success", content = @Content(mediaType = "application/json",
                    schema = @Schema(example =
                            """
                                    {
                                        "attachment_id": "56becc53-9aba-48cc-a3dc-96ca05c357a4"
                                    }"""
                    ))),
            @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(mediaType = "application/json",
                    schema = @Schema(example =
                            """
                                    {
                                        "timestamp": "2023-09-04T18:56:58.271+00:00",
                                        "status": 400,
                                        "error": "Bad Request",
                                        "message": "400 BAD_REQUEST",
                                        "path": "/attachments/",
                                        "fields": {
                                            "reference_type": "Bad reference type",
                                            "reference_id": "Dish with id 0b4b702a-fff6-4682-bba-92075cc7cab not found",
                                            "file_type": "Support only jpg, png, heic, svg"
                                        }
                                    }"""
                    )))
    })
    public ResponseEntity<AttachmentResponseId> createAttachment(@RequestBody @Schema(example = """
            {
              "attachment": "base64",
              "reference_type": "DISH",
              "reference_id": "uuid"
            }""") Attachment request, @RequestHeader HttpHeaders httpHeaders) {
        String acceptLanguage = httpHeaders.getFirst("Accept-Language");
        AttachmentResponseId response = service.createAttachment(request, acceptLanguage);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{attachmentId}")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success", content = @Content(mediaType = "application/json",
                    schema = @Schema(example =
                            """
                                    {
                                        "attachment_id": "56becc53-9aba-48cc-a3dc-96ca05c357a4",
                                        "reference_id": "0b4b702a-fff6-4682-bb2a-9207c50c7cab",
                                        "reference_type": "DISH",
                                        "attachment_url": "https://attachments.obox.pp.ua/b7830129-53f1-4e1b-8a55-398462697ac0/56becc53-9aba-48cc-a3dc-96ca05c357a4.jpg"
                                    }"""

                    ))),
            @ApiResponse(responseCode = "404", description = "Not found", content = @Content(mediaType = "application/json",
                    schema = @Schema(example =
                            """
                                    {
                                        "timestamp": "2023-09-04T19:34:45.293+00:00",
                                        "status": 404,
                                        "error": "Not Found",
                                        "message": "Attachment with id 901b62f9-1b36-4aa8-85cd-4aaecd9e4x051 not found",
                                        "path": "/attachments/901b62f9-1b36-4aa8-85cd-4aaecd9e4x051"
                                    }"""

                    )))
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
                    schema = @Schema(example =
                            """
                                    {
                                        "timestamp": "2023-09-04T19:34:45.293+00:00",
                                        "status": 404,
                                        "error": "Not Found",
                                        "message": "Attachment with id 901b62f9-1b36-4aa8-85cd-4aaecd9e4x051 not found",
                                        "path": "/attachments/901b62f9-1b36-4aa8-85cd-4aaecd9e4x051"
                                    }"""
                    )))
    })
    public ResponseEntity<Void> deleteAttachmentById(@PathVariable String attachmentId, @RequestHeader HttpHeaders httpHeaders) {
        String acceptLanguage = httpHeaders.getFirst("Accept-Language");
        service.deleteAttachmentById(attachmentId, acceptLanguage);
        return ResponseEntity.noContent().build();
    }
}
