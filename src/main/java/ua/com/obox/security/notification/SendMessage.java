package ua.com.obox.security.notification;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

public class SendMessage {
    private final static String apiToken = "6804603259:AAGpqi-O9AlLhDBx8shkbRerlLyRUI9L3_s";
    private final static String chatId = "110085037";
    public static void sendToTelegram(String messageToAdmin) {
        String urlString = "https://api.telegram.org/bot%s/sendMessage?chat_id=%s&text=%s";

        urlString = String.format(urlString, apiToken, chatId, messageToAdmin);
        try {
            URL url = new URL(urlString);
            URLConnection conn = url.openConnection();
            InputStream is = new BufferedInputStream(conn.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
