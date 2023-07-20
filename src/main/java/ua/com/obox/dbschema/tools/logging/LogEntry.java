package ua.com.obox.dbschema.tools.logging;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.Date;

@Data
@NoArgsConstructor
@Entity
@Table(name = "log_entry")
public class LogEntry {
    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(columnDefinition = "CHAR(36)")
    private String id;

    @Enumerated(EnumType.STRING)
    private LogLevel level;
    @Column(length = 1000)
    private String message;

    @Column(name = "query_time")
    private Date queryTime = new Date();

    public LogEntry(LogLevel level, String message) {
        this.level = level;
        this.message = message;
    }
}