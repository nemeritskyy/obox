package ua.com.obox.dbschema.tools.ftp;

import java.io.*;
import java.util.Base64;

import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPSClient;
import org.apache.commons.net.util.TrustManagerUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ua.com.obox.dbschema.tools.Validator;
import ua.com.obox.dbschema.tools.logging.LogLevel;
import ua.com.obox.dbschema.tools.logging.LoggingService;

@Service
public class UploadDishImageFTP {
    private static String loggingMessage;
    private static FTPConfiguration ftpConfiguration = null;

    @Autowired
    public UploadDishImageFTP(FTPConfiguration ftpConfiguration) {
        this.ftpConfiguration = ftpConfiguration;
    }

    public String uploadImage(String image, String path, String uuid, LoggingService loggingService) {
        String[] imageCheckMetaData = image.split(",");
        byte[] imageData = new byte[0];
        try {
            imageData = imageCheckMetaData.length == 1 ? Base64.getDecoder().decode(image.getBytes()) : Base64.getDecoder().decode(imageCheckMetaData[1]);
        } catch (Exception ex) {
            loggingService.log(LogLevel.ERROR, ex + " uuid=" + uuid);
            ex.printStackTrace();
        }
        String fileType = Validator.detectImageType(imageData, loggingService);
        if (fileType != null) {
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

                if (!ftpClient.changeWorkingDirectory(path)) {
                    ftpClient.makeDirectory(path);

                }

                ftpClient.changeWorkingDirectory(path);

                loggingMessage = "Start uploading file: " + fileName;
                boolean done = ftpClient.storeFile(fileName, inputStream);
                System.out.println("load to: " + fileName);
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
        return null;
    }

    public static void deleteImage(String path, String fileName) {
        FTPSClient ftpClient = new FTPSClient();
        ftpClient.setTrustManager(TrustManagerUtils.getAcceptAllTrustManager());
        try {
            ftpClient.connect(ftpConfiguration.getServer(), ftpConfiguration.getPort());
            ftpClient.login(ftpConfiguration.getUser(), ftpConfiguration.getPass());

            ftpClient.enterLocalPassiveMode();
            ftpClient.changeWorkingDirectory(path);
            boolean deleted = ftpClient.deleteFile(fileName);

            if (deleted) {
                ftpClient.changeToParentDirectory();
                FTPFile[] files = ftpClient.listFiles(path);

                if (files != null && files.length == 2) {
                    ftpClient.removeDirectory(path);
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
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
