package ua.com.obox.authserver.config;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Log4jConfiguration {
    @Bean
    public Logger logger() {
        return LogManager.getLogger();
    }
}