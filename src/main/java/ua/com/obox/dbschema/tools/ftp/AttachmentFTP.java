package ua.com.obox.dbschema.tools.ftp;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPSClient;
import org.apache.commons.net.util.TrustManagerUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import ua.com.obox.dbschema.dish.Dish;
import ua.com.obox.dbschema.dish.DishRepository;
import ua.com.obox.dbschema.tools.Validator;
import ua.com.obox.dbschema.tools.attachment.ImageUtils;
import ua.com.obox.dbschema.tools.attachment.ReferenceType;
import ua.com.obox.dbschema.tools.configuration.ValidationConfiguration;
import ua.com.obox.dbschema.tools.logging.LogLevel;
import ua.com.obox.dbschema.tools.logging.LoggingService;
import ua.com.obox.dbschema.tools.response.BadFieldsResponse;
import ua.com.obox.dbschema.tools.response.ResponseErrorMap;

import javax.imageio.ImageIO;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;
import java.util.Map;
import java.util.ResourceBundle;

@Service
public class AttachmentFTP {
    private final DishRepository dishRepository;
    private static String loggingMessage;
    private static FTPConfiguration ftpConfiguration;
    private static LoggingService staticLoggingService;
    private final ResourceBundle translation = ResourceBundle.getBundle("translation.messages");

    @Autowired
    public AttachmentFTP(FTPConfiguration ftpConfiguration, DishRepository dishRepository, LoggingService loggingService) {
        this.dishRepository = dishRepository;
        staticLoggingService = loggingService;
        AttachmentFTP.ftpConfiguration = ftpConfiguration;
    }

    public String uploadAttachment(String attachment, String referenceId, String referenceType, String attachmentUUID, String acceptLanguage) throws IOException {
        Map<String, String> fieldErrors = new ResponseErrorMap<>();
        String path = "bad-associated";
        String fileType;
        byte[] imageData;

        if (attachment.getBytes().length > ValidationConfiguration.MAX_FILE_SIZE * 4 / 3) {
            fieldErrors.put("file_size", String.format(translation.getString(acceptLanguage + ".badFileSize"), ValidationConfiguration.MAX_FILE_SIZE / 1_048_576));
            throw new BadFieldsResponse(HttpStatus.BAD_REQUEST, fieldErrors);
        } else {
            imageData = decodeImageData(attachment);
            fileType = Validator.detectImageType(imageData, staticLoggingService);
            if (fileType == null) {
                fieldErrors.put("file_type", translation.getString(acceptLanguage + ".badFileType"));
            } else {
//                if (attachment.getBytes().length > ValidationConfiguration.ATTACHMENT_COMPRESSING_SIZE && fileType != ".svg" && fileType != ".heic") { // Compress attachment
                if (!fileType.equals(".svg") && !fileType.equals(".heic") && ImageIO.read(new ByteArrayInputStream(imageData)).getWidth() >= ValidationConfiguration.ATTACHMENT_RECOMMENDED_WIDTH) { // Compress attachment
                    imageData = ImageUtils.resizeImageByWidth(imageData, fileType.replaceFirst(".", ""));
                } else if (!fileType.equals(".svg") && !fileType.equals(".heic")){
                    fieldErrors.put("file_size", String.format(translation.getString(acceptLanguage + ".badFileRecommendedWidth"), ValidationConfiguration.ATTACHMENT_RECOMMENDED_WIDTH));
                }
            }
        }

        if (ReferenceType.DISH.toString().equals(Validator.removeExtraSpaces(referenceType).toUpperCase())) {
            path = getDishAssociatedPath(referenceId, acceptLanguage, fieldErrors);
        }

        if (!isValidReferenceType(Validator.removeExtraSpaces(referenceType).toUpperCase())) {
            fieldErrors.put("reference_type", translation.getString(acceptLanguage + ".badReferenceType"));
        }

        if (fieldErrors.size() > 0) {
            throw new BadFieldsResponse(HttpStatus.BAD_REQUEST, fieldErrors);
        }

        if (fileType != null) {
            String fileName = attachmentUUID + fileType;
            uploadFileToFTP(path, fileName, imageData);
            return path + "/" + fileName;
        }
        return null;
    }

    private String getDishAssociatedPath(String referenceId, String acceptLanguage, Map<String, String> fieldErrors) {
        var dishInfo = dishRepository.findByDishId(referenceId);
        Dish dish = dishInfo.orElseGet(() -> {
            fieldErrors.put("reference_id", String.format(translation.getString(acceptLanguage + ".dishNotFound"), referenceId));
            return null;
        });
        return dish != null ? dish.getAssociatedId() : "bad-associated";
    }

    private boolean isValidReferenceType(String referenceType) {
        for (ReferenceType type : ReferenceType.values()) {
            if (type.name().equals(referenceType)) {
                return true;
            }
        }
        return false;
    }

    private byte[] decodeImageData(String attachment) {
        String[] imageCheckMetaData = attachment.split(",");
        return imageCheckMetaData.length == 1 ? Base64.getDecoder().decode(attachment.getBytes()) : Base64.getDecoder().decode(imageCheckMetaData[1]);
    }

    private void uploadFileToFTP(String path, String fileName, byte[] imageData) {
        FTPSClient ftpClient = new FTPSClient();
        ftpClient.setTrustManager(TrustManagerUtils.getAcceptAllTrustManager());

        try {
            loggingMessage = "Before connecting to FTP server";
            ftpClient.connect(ftpConfiguration.getServer(), ftpConfiguration.getPort());
            loggingMessage = "Logging in to FTP server";
            ftpClient.login(ftpConfiguration.getUser(), ftpConfiguration.getPass());
            loggingMessage = "Logged in to FTP server";

            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
            ftpClient.enterLocalPassiveMode();

            InputStream inputStream = new ByteArrayInputStream(imageData);

            if (!ftpClient.changeWorkingDirectory(path)) {
                ftpClient.makeDirectory(path);
            }

            ftpClient.changeWorkingDirectory(path);

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
            staticLoggingService.log(LogLevel.INFO, loggingMessage);
            try {
                if (ftpClient.isConnected()) {
                    ftpClient.logout();
                    ftpClient.disconnect();
                }
            } catch (IOException ex) {
                staticLoggingService.log(LogLevel.ERROR, String.valueOf(ex));
                ex.printStackTrace();
            }
        }
    }

    public static void deleteAttachment(String attachmentUrl) {
        String[] fullPath = attachmentUrl.split("/");
        String path = fullPath[0];
        String fileName = fullPath[1];
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
