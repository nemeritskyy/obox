package ua.com.obox.security.notification;

import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.telegram.telegrambots.meta.api.objects.Update;
import ua.com.obox.authserver.user.Role;
import ua.com.obox.authserver.user.User;
import ua.com.obox.authserver.user.UserRepository;
import ua.com.obox.dbschema.tools.logging.LogLevel;
import ua.com.obox.dbschema.tools.logging.LoggingService;
import ua.com.obox.security.bucket4j.RateLimitingAspect;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Hidden
@RestController
@RequiredArgsConstructor
public class WebhookController {
    private final LoggingService loggingService;
    private final UserRepository userRepository;

    @PostMapping("/notification-webhook")
    public ResponseEntity<String> handleWebhook(@RequestBody Update payload) {
        try {
            onUpdateReceived(payload);
        } catch (Exception e) {
            System.out.println("Bad payload");
        }
        loggingService.log(LogLevel.BOT, payload.toString());
        return ResponseEntity.ok("OK");
    }

    private void onUpdateReceived(Update update) {
        String message = update.getMessage().getText();
        Long chatId = update.getMessage().getChatId();
        if (message.startsWith("/unblock") && isAllowed(chatId)) {
            String regex = "^((25[0-5]|(2[0-4]|1\\d|[1-9]|)\\d)\\.?\\b){4}$";

            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(message.substring(8));
            if (matcher.find()) {
                String ip = matcher.group(0);
                if (RateLimitingAspect.blackList.containsKey(ip)) {
                    RateLimitingAspect.blackList.remove(ip);
                    SendMessage.sendToTelegram(String.format("\u2705 IP:%s UNBLOCKED by USER: %s (%s)", ip, update.getMessage().getFrom().getFirstName(), update.getMessage().getFrom().getUserName()));
                } else {
                    SendMessage.sendToTelegram(String.format("\uD83D\uDEA8 IP:%s NOT FOUND by USER: %s (%s)", ip, update.getMessage().getFrom().getFirstName(), update.getMessage().getFrom().getUserName()));
                }
            } else {
                SendMessage.sendToTelegram("\uD83D\uDEA8\uD83D\uDEA8\uD83D\uDEA8 WRONG FORMAT,\nEXAMPLE TO UNBLOCK WRITE:\n/unblock127.0.0.1");
            }
        }
        if ((message.startsWith("/start") || (message.equals("/help")) && isAllowed(chatId))) {
            String welcome = "**Available commands:**\nAll commands - /help\nUnblock example - /unblock127.0.0.1\n/setadmin your@email.com";
            SendMessage.forwardTelegram(chatId, welcome);
        }
        if (message.startsWith("/setadmin") && isAllowed(chatId)) {
            String regex = "^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$";

            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(message.replace("/setadmin", "").replaceAll(" ", ""));
            if (matcher.find()) {
                String userEmail = matcher.group(0);
                Optional<User> user = userRepository.findByEmail(userEmail);
                if (user.isPresent()) {
                    User userToAdmin = user.get();
                    userToAdmin.setRole(Role.ADMIN);
                    userRepository.save(userToAdmin);
                    SendMessage.sendToTelegram(String.format("\u2705 USER :%s HAS RECEIVED ADMINISTRATOR RIGHTS", userEmail));
                } else {
                    SendMessage.sendToTelegram(String.format("\uD83D\uDEA8\uD83D\uDEA8\uD83D\uDEA8 USER %s NOT FOUND", userEmail));
                }
            } else {
                SendMessage.sendToTelegram("\uD83D\uDEA8\uD83D\uDEA8\uD83D\uDEA8 WRONG EMAIL FORMAT,\nEXAMPLE:\n/setadmin your@email.com");
            }
        }
    }

    private boolean isAllowed(Long chatId) {
        return SendMessage.chatsId.contains(String.valueOf(chatId));
    }
}

