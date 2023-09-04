package ua.com.obox.dbschema.attachment;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AttachmentRepository  extends JpaRepository<Attachment, UUID> {
    Optional<Attachment> findByAttachmentId(String attachmentId);
    List<Attachment> findAllByReferenceId(String entityId);
}
