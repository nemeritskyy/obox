package ua.com.obox.dbschema.tools.attachment;

import org.imgscalr.Scalr;
import ua.com.obox.dbschema.tools.configuration.ValidationConfiguration;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import javax.imageio.ImageIO;

public class ImageUtils {
    public static byte[] resizeImageByWidth(byte[] imageData, String imageType) throws IOException {
        int targetWidth = ValidationConfiguration.ATTACHMENT_RECOMMENDED_WIDTH;
        BufferedImage originalImage = ImageIO.read(new ByteArrayInputStream(imageData));
        BufferedImage resizedImage = resizeImage(originalImage, targetWidth);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(resizedImage, imageType, baos);
        return baos.toByteArray();
    }

    private static BufferedImage resizeImage(BufferedImage originalImage, int targetWidth) {
        return Scalr.resize(originalImage, Scalr.Method.ULTRA_QUALITY, Scalr.Mode.FIT_TO_WIDTH, targetWidth);
    }

    public static byte[] resizeImageByFileSize(byte[] imageData, String imageType) throws IOException {
        BufferedImage originalImage = ImageIO.read(new ByteArrayInputStream(imageData));

        int targetWidth = originalImage.getWidth();
        int targetHeight = originalImage.getHeight();
        double scaleFactor = 1.0;

        do {
            targetWidth = (int) (targetWidth * scaleFactor);
            targetHeight = (int) (targetHeight * scaleFactor);

            BufferedImage resizedImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
            resizedImage.getGraphics().drawImage(originalImage, 0, 0, targetWidth, targetHeight, null);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ImageIO.write(resizedImage, imageType, outputStream);
            byte[] resizedImageData = outputStream.toByteArray();

            if (resizedImageData.length > ValidationConfiguration.ATTACHMENT_COMPRESSING_SIZE) {
                scaleFactor -= 0.05;
            } else {
                return resizedImageData;
            }
        } while (scaleFactor > 0.1);
        return imageData;
    }
}