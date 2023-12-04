package ua.com.obox.security.notification.telegram;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;
import ua.com.obox.security.bucket4j.RateLimitingAspect;
import ua.com.obox.security.notification.SendMessage;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SecurityBot extends TelegramLongPollingBot {
    @Override
    public void onUpdateReceived(Update update) {
        String message = update.getMessage().getText();
        if (message.contains("/unblock")) {
            String regex = "((?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?\\.){3}(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)))";

            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(message);
            if (matcher.find()) {
                String ip = matcher.group(1);
                if (RateLimitingAspect.blackList.containsKey(ip)) {
                    RateLimitingAspect.blackList.remove(ip);
                    SendMessage.sendToTelegram(String.format("\u2705 IP:%s UNBLOCKED", ip));
                }
            } else {
                SendMessage.sendToTelegram("\uD83D\uDEA8\uD83D\uDEA8\uD83D\uDEA8 WRONG FORMAT,\nEXAMPLE TO UNBLOCK WRITE:\n/unblock127.0.0.1");
            }
        }
    }

    @Override
    public String getBotUsername() {
        return "OboxSecurity";
    }

    @Override
    public String getBotToken() {
        return "6804603259:AAGpqi-O9AlLhDBx8shkbRerlLyRUI9L3_s";
    }
}
