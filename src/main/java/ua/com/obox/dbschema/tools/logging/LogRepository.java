package ua.com.obox.dbschema.tools.logging;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface LogRepository extends JpaRepository<LogEntry, UUID> {
}
