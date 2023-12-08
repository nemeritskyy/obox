package ua.com.obox;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import ua.com.obox.security.notification.telegram.SecurityBot;

import java.util.TimeZone;

@SpringBootApplication
public class OboxApplication extends SpringBootServletInitializer {
    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        return builder.sources(OboxApplication.class);
    }

    public static void main(String[] args) throws TelegramApiException {
        TimeZone.setDefault(TimeZone.getTimeZone("Europe/Kiev"));
        if (System.getenv().containsKey("telegram")) {
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(new SecurityBot());
        }
        SpringApplication.run(OboxApplication.class, args);
    }

//    @Bean
//    public CommandLineRunner commandLineRunner(
//            AuthenticationService service
//    ) {
//        return args -> {
//            var admin = RegisterRequest.builder()
//                    .firstname("Admin")
//                    .lastname("Admin")
//                    .email("admin@mail.com")
//                    .password("Aa8*password")
//                    .role(ADMIN)
//                    .build();
//            System.out.println("Admin token: " + service.register(admin));
//
//            var manager = RegisterRequest.builder()
//                    .firstname("Admin")
//                    .lastname("Manager")
//                    .email("manager@mail.com")
//                    .password("Aa8*password")
//                    .role(MANAGER)
//                    .build();
//            System.out.println("Manager token: " + service.register(manager));
//        };
//    }
}