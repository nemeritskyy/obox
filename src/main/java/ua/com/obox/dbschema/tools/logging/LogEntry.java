package ua.com.obox.dbschema.tools.logging;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.Date;

@Data
@AllArgsConstructor
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

    @Column(columnDefinition = "CHAR(15)")
    private String ip;

    @Lob
    private String message;

    @Column(name = "server_time")
    private Date serverTime;

    @Column(name = "unix_time")
    private long unixTime;


}