package ua.com.obox.dbschema.tools.attachment;

import org.springframework.stereotype.Service;
import ua.com.obox.dbschema.attachment.AttachmentRepository;
import ua.com.obox.dbschema.dish.Dish;

@Service
public class AttachmentTools {
    public static String getURL(Dish dish, AttachmentRepository attachmentRepository, String attachmentsDns) {
        if (dish.getImage() != null) {
            var attachment = attachmentRepository.findByAttachmentId(dish.getImage());
            if (attachment.isPresent()) {
                return String.format("%s/%s", attachmentsDns, attachment.get().getAttachmentUrl());
            }
        }
        return null;
    }
}
