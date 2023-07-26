package ua.com.obox.dbschema.tools.ftp;

import java.io.*;
import java.util.Base64;

import org.apache.commons.net.ftp.FTPSClient;
import org.apache.commons.net.util.TrustManagerUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ua.com.obox.dbschema.tools.Validator;
import ua.com.obox.dbschema.tools.logging.LogLevel;
import ua.com.obox.dbschema.tools.logging.LoggingService;

@Service
public class UploadItemImageFTP {
    private static String loggingMessage;
    private final FTPConfiguration ftpConfiguration;

    @Autowired
    public UploadItemImageFTP(FTPConfiguration ftpConfiguration) {
        this.ftpConfiguration = ftpConfiguration;
    }

    public String uploadImage(String image, String uuid, LoggingService loggingService) {
        byte[] imageData = Base64.getDecoder().decode(image);
        String fileType = Validator.detectImageType(imageData, loggingService);
        String fileName = uuid + fileType;

        FTPSClient ftpClient = new FTPSClient();
        ftpClient.setTrustManager(TrustManagerUtils.getAcceptAllTrustManager());
        try {
            loggingMessage = "Before connecting to FTP server";
            ftpClient.connect(ftpConfiguration.getServer(), ftpConfiguration.getPort());
            loggingMessage = "Logging in to FTP server";
            ftpClient.login(ftpConfiguration.getUser(), ftpConfiguration.getPass());
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
                loggingService.log(LogLevel.ERROR, ex + " uuid=" + uuid);
                ex.printStackTrace();
            }
        }
        return fileName;
    }

    public void deleteImage(String fileName, LoggingService loggingService) {
        FTPSClient ftpClient = new FTPSClient();
        ftpClient.setTrustManager(TrustManagerUtils.getAcceptAllTrustManager());
        try {
            loggingMessage = "Before connecting to FTP server";
            ftpClient.connect(ftpConfiguration.getServer(), ftpConfiguration.getPort());
            loggingMessage = "Logging in to FTP server";
            ftpClient.login(ftpConfiguration.getUser(), ftpConfiguration.getPass());
            loggingMessage = "Start deleting file: " + fileName;
            boolean deleted = ftpClient.deleteFile(fileName);
            if (deleted) {
                loggingMessage = "The file " + fileName + " is deleted successfully.";
            } else {
                loggingMessage = "Failed to delete the file " + fileName;
            }
        } catch (IOException ex) {
            loggingMessage = "Error: " + ex.getMessage();
            ex.printStackTrace();
        } finally {
            loggingService.log(LogLevel.INFO, loggingMessage);
            try {
                if (ftpClient.isConnected()) {
                    ftpClient.logout();
                    ftpClient.disconnect();
                }
            } catch (IOException ex) {
                loggingService.log(LogLevel.ERROR, ex.toString());
                ex.printStackTrace();
            }
        }
    }


}
