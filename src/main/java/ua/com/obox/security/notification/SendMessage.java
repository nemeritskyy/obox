package ua.com.obox.security.notification;

import org.springframework.web.util.UriComponentsBuilder;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.List;

public class SendMessage {
    private final static String apiToken = "6804603259:AAGpqi-O9AlLhDBx8shkbRerlLyRUI9L3_s";

    private final static List<String> chatsId = List.of(
            "110085037", // andrew
            "6935742919" // andrew test
    );

    public static void sendToTelegram(String messageToAdmin) {
        String message = URLEncoder.encode(messageToAdmin);
        for (String telegramId : chatsId) {
            String urlString = UriComponentsBuilder
                    .fromUriString("https://api.telegram.org/bot" + apiToken + "/sendMessage")
                    .queryParam("chat_id", telegramId)
                    .queryParam("text", message)
                    .build()
                    .toUriString();

            try {
                URL url = new URL(urlString);
                URLConnection conn = url.openConnection();
                InputStream is = new BufferedInputStream(conn.getInputStream());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}