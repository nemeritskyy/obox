package ua.com.obox.security.notification;

import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import ua.com.obox.dbschema.tools.logging.LoggingService;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class LogSenderTask implements Runnable {
    @Override
    public void run() {
        try {
            sendLogFile();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendLogFile() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        String todayDate = dateFormat.format(new Date());
        String logFilePath = LoggingService.logPath + File.separator + todayDate + ".log";
        File logFile = new File(logFilePath);
        InputFile inputFile = new InputFile(logFile);
        SendDocument sendDocumentRequest = new SendDocument();
        sendDocumentRequest.setDocument(inputFile);
//        sendDocument(sendDocumentRequest);
    }
}
