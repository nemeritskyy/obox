package ua.com.obox.dbschema.tools.ftp;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties
@Getter
public class FTPConfiguration {
    private final String server;
    private final int port;
    private final String user;
    private final String pass;

    public FTPConfiguration(@Value("${ftp.server}") String server,
                            @Value("${ftp.port}") int port,
                            @Value("${ftp.username}") String user,
                            @Value("${ftp.password}") String pass) {
        this.server = server;
        this.port = port;
        this.user = user;
        this.pass = pass;
    }
}