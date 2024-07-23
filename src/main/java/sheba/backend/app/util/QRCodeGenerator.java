package sheba.backend.app.util;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class QRCodeGenerator {

    public static String generateQRCode(String qrName, String content, String filePath, String imgPath) throws WriterException, IOException {
        BufferedImage qrImage = generateQRCodeImage(qrName, content, imgPath);
        String qrCodeName = qrName + "-QRCODE.png";
        Path directoryPath = Paths.get(filePath);
        if (!Files.exists(directoryPath)) {
            Files.createDirectories(directoryPath);
        }
        Path fullPath = directoryPath.resolve(qrCodeName);
        ImageIO.write(qrImage, "PNG", fullPath.toFile());
        return fullPath.toString();
    }

    public static void generateQRCode(String qrName, String content, OutputStream outputStream, String imgPath) throws WriterException, IOException {
        BufferedImage qrImage = generateQRCodeImage(qrName, content, imgPath);
        ImageIO.write(qrImage, "PNG", outputStream);
    }

    private static BufferedImage generateQRCodeImage(String qrName, String content, String imgPath) throws WriterException, IOException {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
        BitMatrix bitMatrix = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, 400, 400, hints);

        BufferedImage qrImage = MatrixToImageWriter.toBufferedImage(bitMatrix);

        if (imgPath != null && !imgPath.isEmpty()) {
            File logoFile = new File(imgPath);
            if (logoFile.exists()) {
                try {
                    BufferedImage logo = ImageIO.read(logoFile);
                    int logoSize = qrImage.getWidth() / 3; // img size relative to qr
                    Image scaledLogo = logo.getScaledInstance(logoSize, logoSize, Image.SCALE_SMOOTH);
                    BufferedImage logoResized = new BufferedImage(logoSize, logoSize, BufferedImage.TYPE_INT_ARGB);

                    Graphics2D g2d = logoResized.createGraphics();
                    g2d.drawImage(scaledLogo, 0, 0, null);
                    g2d.dispose();

                    int deltaHeight = qrImage.getHeight() - logoResized.getHeight();
                    int deltaWidth = qrImage.getWidth() - logoResized.getWidth();

                    BufferedImage combined = new BufferedImage(qrImage.getHeight(), qrImage.getWidth(), BufferedImage.TYPE_INT_ARGB);
                    Graphics2D g = (Graphics2D) combined.getGraphics();
                    g.drawImage(qrImage, 0, 0, null);
                    g.drawImage(logoResized, Math.round(deltaWidth / 2), Math.round(deltaHeight / 2), null);

                    return combined;
                } catch (IOException e) {
                    // If there's an error reading the logo, just return the QR code without logo
                    System.err.println("Error reading logo file: " + e.getMessage());
                }
            } else {
                System.err.println("Logo file not found: " + imgPath);
            }
        }

        // If no logo was added, return the QR code without logo
        return qrImage;
    }
}