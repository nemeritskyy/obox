package ua.com.obox.security.notification;

import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.telegram.telegrambots.meta.api.objects.Update;
import ua.com.obox.dbschema.tools.logging.LogLevel;
import ua.com.obox.dbschema.tools.logging.LoggingService;
import ua.com.obox.security.bucket4j.RateLimitingAspect;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Hidden
@RestController
@RequiredArgsConstructor
public class WebhookController {
    private final LoggingService loggingService;

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
        if (message.contains("/unblock") && SendMessage.chatsId.contains(String.valueOf(update.getMessage().getChatId()))) {
            String regex = "((?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?\\.){3}(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)))";

            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(message);
            if (matcher.find()) {
                String ip = matcher.group(1);
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
    }
}

