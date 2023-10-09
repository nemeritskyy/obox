package ua.com.obox.dbschema.tools.attachment;

import org.imgscalr.Scalr;
import ua.com.obox.dbschema.tools.configuration.ValidationConfiguration;

import java.awt.image.BufferedImage;
import java.io.*;
import javax.imageio.ImageIO;

public class ImageUtils {
    private static final int BUFFER_SIZE = 8192;

    public static byte[] resizeImageByWidth(byte[] imageData, String imageType) throws IOException {
        int targetWidth = ValidationConfiguration.ATTACHMENT_RECOMMENDED_WIDTH;

        try (ByteArrayInputStream bais = new ByteArrayInputStream(imageData);
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            BufferedImage originalImage = ImageIO.read(bais);

            if (originalImage != null) {
                BufferedImage resizedImage = resizeImage(originalImage, targetWidth);
                byte[] buffer = new byte[BUFFER_SIZE];
                try (ByteArrayOutputStream tempBaos = new ByteArrayOutputStream()) {
                    ImageIO.write(resizedImage, imageType, new NoCloseOutputStream(tempBaos));
                    try (ByteArrayInputStream tempBais = new ByteArrayInputStream(tempBaos.toByteArray())) {
                        int bytesRead;
                        while ((bytesRead = tempBais.read(buffer)) != -1) {
                            baos.write(buffer, 0, bytesRead);
                        }
                    }
                }
                return baos.toByteArray();
            } else {
                throw new IOException("Failed to read the original image.");
            }
        }
    }

    private static class NoCloseOutputStream extends FilterOutputStream {
        public NoCloseOutputStream(OutputStream out) {
            super(out);
        }

        @Override
        public void close() throws IOException {
        }
    }

    private static BufferedImage resizeImage(BufferedImage originalImage, int targetWidth) {
        return Scalr.resize(originalImage, Scalr.Method.ULTRA_QUALITY, Scalr.Mode.FIT_TO_WIDTH, targetWidth);
    }
}