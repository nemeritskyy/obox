package ua.com.obox.dbschema.attachment;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ua.com.obox.dbschema.dish.Dish;
import ua.com.obox.dbschema.dish.DishRepository;
import ua.com.obox.dbschema.tools.Validator;
import ua.com.obox.dbschema.tools.exception.ExceptionTools;
import ua.com.obox.dbschema.tools.ftp.AttachmentFTP;
import ua.com.obox.dbschema.tools.translation.CheckHeader;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AttachmentService {
    private final AttachmentRepository attachmentRepository;
    private final DishRepository dishRepository;
    private final AttachmentFTP attachmentFTP;
    @Value("${application.image-dns}")
    private String attachmentsDns;

    public List<AttachmentResponse> getAllAttachmentsByEntityId(String entityId, String acceptLanguage) {
        List<Attachment> attachments = attachmentRepository.findAllByReferenceId(entityId);

        return attachments.stream()
                .map(attachment -> AttachmentResponse.builder()
                        .attachmentId(attachment.getAttachmentId())
                        .referenceId(attachment.getReferenceId())
                        .referenceType(attachment.getReferenceType())
                        .attachmentUrl(String.format("%s/%s", attachmentsDns, attachment.getAttachmentUrl()))
                        .build())
                .collect(Collectors.toList());
    }

    public AttachmentResponse getAttachmentById(String attachmentId, String acceptLanguage) {
        String finalAcceptLanguage = CheckHeader.checkHeaderLanguage(acceptLanguage);
        var attachmentInfo = attachmentRepository.findByAttachmentId(attachmentId);

        Attachment attachment = attachmentInfo.orElseThrow(() -> {
            ExceptionTools.notFoundResponse(".attachmentNotFound", finalAcceptLanguage, attachmentId);
            return null;
        });

        return AttachmentResponse.builder()
                .attachmentId(attachment.getAttachmentId())
                .referenceId(attachment.getReferenceId())
                .referenceType(attachment.getReferenceType())
                .attachmentUrl(String.format("%s/%s", attachmentsDns, attachment.getAttachmentUrl()))
                .build();
    }

    public AttachmentResponseId createAttachment(Attachment request, String acceptLanguage) {
        String finalAcceptLanguage = CheckHeader.checkHeaderLanguage(acceptLanguage);
        String attachmentUUID = String.valueOf(UUID.randomUUID());
        String attachmentUrl = attachmentFTP.uploadAttachment(request.getAttachment(), request.getReferenceId(), request.getReferenceType(), attachmentUUID, finalAcceptLanguage);
        Attachment attachment = Attachment.builder()
                .attachmentId(attachmentUUID)
                .referenceId(request.getReferenceId())
                .referenceType(Validator.removeExtraSpaces(request.getReferenceType()).toUpperCase())
                .attachmentUrl(attachmentUrl)
                .build();
        attachmentRepository.save(attachment);
        return AttachmentResponseId.builder()
                .attachmentId(attachment.getAttachmentId())
                .build();
    }

    public void deleteAttachmentById(String attachmentId, String acceptLanguage) {
        String finalAcceptLanguage = CheckHeader.checkHeaderLanguage(acceptLanguage);

        var attachmentInfo = attachmentRepository.findByAttachmentId(attachmentId);

        Attachment attachment = attachmentInfo.orElseThrow(() -> {
            ExceptionTools.notFoundResponse(".attachmentNotFound", finalAcceptLanguage, attachmentId);
            return null;
        });

        attachmentRepository.delete(attachment);

        var dishInfo = dishRepository.findByImage(attachment.getAttachmentId());

        if (dishInfo.isPresent()){
            Dish dish = dishInfo.get();
            dish.setImage(null);
        }

        AttachmentFTP.deleteAttachment(attachment.getAttachmentUrl());
    }
}