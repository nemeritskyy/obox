package ua.com.obox.security.notification.telegram;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;
import ua.com.obox.dbschema.tools.attachment.ApplicationContextProvider;
import ua.com.obox.security.notification.WebhookController;


public class SecurityBot extends TelegramLongPollingBot {
    private final WebhookController webhookController = ApplicationContextProvider.getBean(WebhookController.class);
    private final String botUsername;

    public SecurityBot(String botToken, String botName) {
        super(botToken);
        this.botUsername = botName;
    }

    @Override
    public void onUpdateReceived(Update update) {
        webhookController.onUpdateReceived(update);
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }

}
