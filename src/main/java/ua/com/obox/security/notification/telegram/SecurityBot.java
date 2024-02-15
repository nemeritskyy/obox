package ua.com.obox.security.notification.telegram;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ua.com.obox.dbschema.tools.attachment.ApplicationContextProvider;
import ua.com.obox.security.notification.LogSenderTask;
import ua.com.obox.security.notification.SendMessage;
import ua.com.obox.security.notification.WebhookController;

import java.text.ParseException;


public class SecurityBot extends TelegramLongPollingBot {
    private final WebhookController webhookController = ApplicationContextProvider.getBean(WebhookController.class);
    private final String botUsername;

    public SecurityBot(String botToken, String botName) {
        super(botToken);
        this.botUsername = botName;
    }

    @Override
    public void onUpdateReceived(Update update) {
        String command = update.getMessage().getText();
        String chatId = update.getMessage().getChatId().toString();
        if (command.startsWith("/log") && SendMessage.allowChatId.contains(chatId)) {
            try {
                if (command.startsWith("/logfind")) {
                    LogSenderTask.findByRequest(command, chatId);
                } else {
                    this.execute(LogSenderTask.getLastLog(command, chatId));
                }
            } catch (TelegramApiException e) {
                System.out.printf("\nCan't send log {%s}", command);
            } catch (ParseException e) {
                System.out.println(e);
            }
        } else {
            webhookController.onUpdateReceived(update);
        }
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }
}