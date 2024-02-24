package ua.com.obox.dbschema.tools.logging;

import java.io.File;
import java.io.RandomAccessFile;
import java.text.SimpleDateFormat;
import java.util.Date;

public class GetLog {
    public static String getTodayLast(int count) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        String todayDate = dateFormat.format(new Date());
        String logFilePath = LoggingService.logPath + File.separator + todayDate + ".log";
        File logFile = new File(logFilePath);
        if (logFile.exists()) {
            if (count > 100)
                count = 100;
            return readLastNLinesFromFile(logFile, count);
        } else {
            return "can't read log";
        }
    }

    public static String readLastNLinesFromFile(File file, int n) {
        StringBuilder result = new StringBuilder();

        try (RandomAccessFile randomAccessFile = new RandomAccessFile(file, "r")) {
            long fileLength = randomAccessFile.length();
            long pointer = fileLength - 1;

            int lineCount = 0;

            while (pointer >= 0 && lineCount < n) {
                randomAccessFile.seek(pointer);
                char c = (char) randomAccessFile.read();

                if (c == '\n') {
                    lineCount++;
                }

                result.insert(0, c);
                pointer--;
            }

            if (lineCount < n) {
                result.insert(0, "\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result.toString();
    }
}
