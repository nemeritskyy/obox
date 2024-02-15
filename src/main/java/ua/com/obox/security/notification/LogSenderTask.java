package ua.com.obox.security.notification;

import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import ua.com.obox.dbschema.tools.attachment.ApplicationContextProvider;
import ua.com.obox.dbschema.tools.logging.LoggingService;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LogSenderTask {
    static SendMessage sendMessage = ApplicationContextProvider.getBean(SendMessage.class);

    public static void findByRequest(String command, String chatId) {
        command = command.replace("/logfind", "").trim().toLowerCase();

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        String path = LoggingService.logPath + File.separator + dateFormat.format(new Date()) + ".log";
        File logFile = new File(path);
        StringBuilder stringBuilder = new StringBuilder();

        if (!command.isEmpty()) {
            List<String> lines = searchLogFile(logFile, command);
            for (String line : lines) {
                stringBuilder.append(line);
                stringBuilder.append("\n");
            }
        }

        if (stringBuilder.isEmpty()) {
            sendMessage.forwardTelegram(Long.parseLong(chatId), String.format("Not found recording {%s} on %s", command, dateFormat.format(new Date())));
        } else {
            sendMessage.forwardTelegram(Long.parseLong(chatId), stringBuilder.toString());
        }
    }

    private static List<String> searchLogFile(File logFile, String searchRequest) {
        List<String> lines = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(logFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        List<String> matchingLines = new ArrayList<>();
        for (int i = lines.size() - 1; i >= 0; i--) {
            String line = lines.get(i);
            if (line.toLowerCase().contains(searchRequest)) {
                matchingLines.add(line);
                if (matchingLines.size() == 100)
                    return matchingLines;
            }
        }
        return matchingLines;
    }

    public static SendDocument getLastLog(String command, String chatId) throws ParseException {
        command = command.replaceAll("_", "-");
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        String logDate = dateFormat.format(new Date());
        Pattern pattern = Pattern.compile("\\d{2}-\\d{2}-\\d{4}\\b");
        Matcher matcher = pattern.matcher(command);

        while (matcher.find()) {
            logDate = matcher.group();
        }

        String path = LoggingService.logPath + File.separator + logDate + ".log";
        File logFile = new File(path);
        InputFile inputFile = new InputFile(logFile);
        SendDocument sendDocumentRequest = new SendDocument();
        sendDocumentRequest.setDocument(inputFile);
        sendDocumentRequest.setChatId(chatId);
        return logFile.exists() ? sendDocumentRequest : null;
    }
}
