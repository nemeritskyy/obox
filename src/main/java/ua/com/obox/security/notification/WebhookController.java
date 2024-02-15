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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Hidden
@RestController
@RequiredArgsConstructor
public class WebhookController {
    private final LoggingService loggingService;
    private final UserRepository userRepository;
    private final SendMessage sendMessage;

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

    public void onUpdateReceived(Update update) {
        String message = update.getMessage().getText().replaceAll("_", ".");
        Long chatId = update.getMessage().getChatId();
        if (message.startsWith("/unblock") && isAllowed(chatId)) {
            String regex = "^((25[0-5]|(2[0-4]|1\\d|[1-9]|)\\d)\\.?\\b){4}$";

            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(message.substring(8));
            if (matcher.find()) {
                String ip = matcher.group(0);
                if (RateLimitingAspect.blackList.containsKey(ip)) {
                    RateLimitingAspect.blackList.remove(ip);
                    sendMessage.sendToTelegram(String.format("\u2705 IP:%s UNBLOCKED by USER: %s (%s)", ip, update.getMessage().getFrom().getFirstName(), update.getMessage().getFrom().getUserName()));
                } else {
                    sendMessage.sendToTelegram(String.format("\uD83D\uDEA8 IP:%s NOT FOUND by USER: %s (%s)", ip, update.getMessage().getFrom().getFirstName(), update.getMessage().getFrom().getUserName()));
                }
            } else {
                sendMessage.sendToTelegram("\uD83D\uDEA8\uD83D\uDEA8\uD83D\uDEA8 WRONG FORMAT,\nEXAMPLE TO UNBLOCK WRITE:\n/unblock127.0.0.1");
            }
        }
        if ((message.startsWith("/start") || (message.equals("/help")) && isAllowed(chatId))) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
            String logExampleDate = dateFormat.format(new Date());
            String welcome = "**Available commands:**" +
                    "\n/help" +
                    "\n/unblock92_249_92_46 [for fast use] or /unblock127.0.0.1" +
                    "\n/setadmin your@email.com" +
                    "\n/log" +
                    "\n/log" + logExampleDate.replaceAll("-", "_") + " [for fast use] or /log " + logExampleDate +
                    "\n/logfind {request} [return last 100 results]";
            sendMessage.forwardTelegram(chatId, welcome);
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
                    sendMessage.sendToTelegram(String.format("\u2705 USER :%s HAS RECEIVED ADMINISTRATOR RIGHTS", userEmail));
                } else {
                    sendMessage.sendToTelegram(String.format("\uD83D\uDEA8\uD83D\uDEA8\uD83D\uDEA8 USER %s NOT FOUND", userEmail));
                }
            } else {
                sendMessage.sendToTelegram("\uD83D\uDEA8\uD83D\uDEA8\uD83D\uDEA8 WRONG EMAIL FORMAT,\nEXAMPLE:\n/setadmin your@email.com");
            }
        }
    }

    private boolean isAllowed(Long chatId) {
        return sendMessage.allowChatId.contains(String.valueOf(chatId));
    }
}

