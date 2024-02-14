package ua.com.obox.security.notification;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;
import ua.com.obox.dbschema.tools.logging.LogEntry;
import ua.com.obox.dbschema.tools.logging.LogLevel;
import ua.com.obox.dbschema.tools.logging.LoggingService;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class SendMessage {
    @Value("${telegram.bot.token}")
    private String apiToken;

    public final static List<String> chatsId = List.of(
            "110085037" // andrew
            , "688726739" // dari
            , "296348102" // olena
            , "292030593" // anastasia
            , "6935742919" // andrew test
            , "5064058806" // rodion
    );

    public void sendToTelegram(String messageToAdmin) {
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
                LoggingService.addRecordToLog(
                        LogEntry.builder()
                                .level(LogLevel.INFO)
                                .ip("Server")
                                .message(String.format("can't send message to user %s", getChatId(e.getMessage().substring(136))))
                                .serverTime(new Date())
                                .unixTime(Instant.now().getEpochSecond())
                                .build().toString());
            }
        }
    }

    private String getChatId(String url) {
        String[] params = url.split("&");
        Map<String, String> map = new HashMap<String, String>();

        for (String param : params) {
            String name = param.split("=")[0];
            String value = param.split("=")[1];
            map.put(name, value);
        }
        return map.get("chat_id");
    }

    public void forwardTelegram(long messageTo, String messageForward) {
        String message = URLEncoder.encode(messageForward);
        String urlString = UriComponentsBuilder
                .fromUriString("https://api.telegram.org/bot" + apiToken + "/sendMessage")
                .queryParam("chat_id", messageTo)
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