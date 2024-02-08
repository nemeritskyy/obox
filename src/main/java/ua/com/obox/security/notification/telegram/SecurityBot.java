package ua.com.obox.security.notification.telegram;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;

public class SecurityBot extends TelegramLongPollingBot {
    @Override
    public void onUpdateReceived(Update update) {

    }

    @Override
    public String getBotUsername() {
        return "OboxSecurity";
    }

    @Override
    public String getBotToken() {
        return "6804603259:AAF1rsE65CK7Fq0sXouv0HzxH93eNhVOTjE";
    }
}
