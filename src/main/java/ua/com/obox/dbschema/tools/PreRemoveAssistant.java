package ua.com.obox.dbschema.tools;

import ua.com.obox.dbschema.attachment.Attachment;
import ua.com.obox.dbschema.attachment.AttachmentRepository;
import ua.com.obox.dbschema.sorting.EntityOrder;
import ua.com.obox.dbschema.sorting.EntityOrderRepository;
import ua.com.obox.dbschema.tools.attachment.ApplicationContextProvider;
import ua.com.obox.dbschema.tools.ftp.AttachmentFTP;
import ua.com.obox.dbschema.translation.TranslationRepository;

import java.util.List;

public class PreRemoveAssistant {
    public static void removeByEntityId(String entityId) {
        EntityOrderRepository entityOrderRepository = ApplicationContextProvider.getBean(EntityOrderRepository.class);
        entityOrderRepository.findByEntityId(entityId).ifPresent(entityOrderRepository::delete);
        EntityOrder existSorted = entityOrderRepository.findBySortedListContaining(entityId).orElse(null);
        if (existSorted != null) {
            String[] elements = existSorted.getSortedList().split(",");
            StringBuilder result = new StringBuilder();
            for (String element : elements) {
                if (!element.equals(entityId)) {
                    result.append(element).append(",");
                }
            }
            if (result.length() > 0) {
                result.setLength(result.length() - 1);
                existSorted.setSortedList(result.toString());
                entityOrderRepository.save(existSorted);
            } else {
                entityOrderRepository.delete(existSorted);
            }
        }
        TranslationRepository translationRepository = ApplicationContextProvider.getBean(TranslationRepository.class);
        translationRepository.findAllByReferenceId(entityId).ifPresent(translationRepository::delete);
    }

    public static void removeAttachmentByEntityId(String entityId){
        AttachmentRepository attachmentRepository = ApplicationContextProvider.getBean(AttachmentRepository.class);
        List<Attachment> attachments = attachmentRepository.findAllByReferenceId(entityId);
        if (!attachments.isEmpty()) {
            try {
                for (Attachment attachment : attachments) {
                    AttachmentFTP.deleteAttachment(attachment.getAttachmentUrl());
                    attachmentRepository.delete(attachment);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}
