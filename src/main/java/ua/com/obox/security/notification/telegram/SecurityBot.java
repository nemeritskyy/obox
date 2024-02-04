package ua.com.obox.security.notification.telegram;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;

public class SecurityBot extends TelegramLongPollingBot {
    private final String botUsername;

    public SecurityBot(String botToken, String botName) {
        super(botToken);
        this.botUsername = botName;
    }

    @Override
    public void onUpdateReceived(Update update) {

    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }

}
