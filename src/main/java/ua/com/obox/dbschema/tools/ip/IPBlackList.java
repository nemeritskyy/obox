package ua.com.obox.dbschema.tools.ip;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ua.com.obox.security.notification.SendMessage;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
public class IPBlackList {
    private final SendMessage sendMessage;
    public static Map<String, AtomicInteger> blackList = new HashMap<>();

    public List<String> warmRequests;
    @Value("${APP_VERSION}")
    private String appVersion;

    @PostConstruct
    public void init() {
        warmRequests = new ArrayList<>();
        try (BufferedReader ipReader = new BufferedReader(new InputStreamReader(new FileInputStream("src/main/resources/blacklist/ip.txt")));
             BufferedReader maskReader = new BufferedReader(new InputStreamReader(new FileInputStream("src/main/resources/blacklist/warm-mask.txt")))) {

            String ip;
            while ((ip = ipReader.readLine()) != null) {
                blackList.put(ip, new AtomicInteger(11));
            }

            String mask;
            while ((mask = maskReader.readLine()) != null) {
                warmRequests.add(mask);
            }
            System.out.println(warmRequests);

        } catch (IOException e) {
            System.out.println("Blacklist init was failed");
        }

        sendMessage.sendToTelegram(String.format("\uD83D\uDD04 Application was restarted. Deploy ver. %s", appVersion));
    }

    public boolean checkBlackList(String ipAddress) {
        return blackList.containsKey(ipAddress);
    }

    public void addToBlackList(String ipAddress, String requestUrl) {
        blackList.put(ipAddress, new AtomicInteger(11));
        sendMessage.sendToTelegram(String.format("\uD83E\uDEB1 IP:%s BLOCKED,\nLAST REQUEST:%s", ipAddress, requestUrl));
    }
}
