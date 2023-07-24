package ua.com.obox.dbschema.tools.logging;

import java.io.*;
import java.util.Base64;

import org.apache.commons.net.ftp.FTPSClient;
import org.apache.commons.net.util.TrustManagerUtils;
import ua.com.obox.dbschema.tools.Validator;

public class UploadItemImageFTP {
    private static String loggingMessage;
    //    @Value("${ftp.server}")
    private static String server = "obox.com.ua";
    //    @Value("${ftp.port}")
    private static int port = 21;
    //    @Value("${ftp.username}")
    private static String user = "boss@obox.com.ua";
    //    @Value("${ftp.password}")
    private static String pass = "";

    public void uploadImage(String image, String uuid, LoggingService loggingService) throws IOException {
        byte[] imageData = Base64.getDecoder().decode(image);
        String fileType = Validator.detectImageType(imageData);
        String fileName = uuid + fileType;

        FTPSClient ftpClient = new FTPSClient();
        ftpClient.setTrustManager(TrustManagerUtils.getAcceptAllTrustManager());
        try {
            loggingMessage = "Before connecting to FTP server";
            ftpClient.connect(server, port);
            loggingMessage = "Logging in to FTP server";
            ftpClient.login(user, pass);
            loggingMessage = "Logged in to FTP server";

            ftpClient.setFileType(ftpClient.BINARY_FILE_TYPE);
            ftpClient.enterLocalPassiveMode();

            InputStream inputStream = new ByteArrayInputStream(imageData);

            loggingMessage = "Start uploading file: " + fileName;
            boolean done = ftpClient.storeFile(fileName, inputStream);
            inputStream.close();
            if (done) {
                loggingMessage = "The file " + fileName + " is uploaded successfully.";
            } else {
                loggingMessage = "Failed to upload the file " + fileName;
            }
        } catch (IOException ex) {
            loggingMessage = "Error: " + ex.getMessage();
            ex.printStackTrace();
        } finally {
            loggingService.log(LogLevel.INFO, loggingMessage + " uuid=" + uuid);
            try {
                if (ftpClient.isConnected()) {
                    ftpClient.logout();
                    ftpClient.disconnect();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }


}
