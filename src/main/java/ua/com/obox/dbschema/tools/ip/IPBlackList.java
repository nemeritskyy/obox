package ua.com.obox.dbschema.tools.ip;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ua.com.obox.security.notification.SendMessage;

import javax.annotation.PostConstruct;
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
    @PostConstruct
    public void init() {
        warmRequests = new ArrayList<>();
        warmRequests.add(";");
        warmRequests.add(".env");
        warmRequests.add("set_LimitClient_cfg");
        warmRequests.add("cf_scripts/scripts/ajax/ckeditor/ckeditor.js");
        warmRequests.add("dns-query");
        warmRequests.add("actuator");
        warmRequests.add("cgi-bin");
    }

    public boolean checkBlackList(String ipAddress) {
        return blackList.containsKey(ipAddress);
    }

    public void addToBlackList(String ipAddress, String requestUrl) {
        blackList.put(ipAddress,new AtomicInteger(11));
        sendMessage.sendToTelegram(String.format("\uD83E\uDEB1 IP:%s BLOCKED,\nLAST REQUEST:%s", ipAddress, requestUrl, ipAddress));
    }
}
