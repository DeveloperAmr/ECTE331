package image;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.swing.*;

public class HistogramEqualization {

    public static void main(String[] args) {
        try {
            // Load image
            BufferedImage image = ImageIO.read(new File("Rain_Tree.jpg"));

            // Convert to grayscale
            BufferedImage grayImage = convertToGrayscale(image);

            // Equalize histogram
            BufferedImage equalizedImage = equalizeHistogram(grayImage);

            // Display/save image
            displayImage(equalizedImage);
            saveImage(equalizedImage, "equalized.jpg");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static BufferedImage convertToGrayscale(BufferedImage image) {
        BufferedImage grayImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
        Graphics graphics = grayImage.getGraphics();
        graphics.drawImage(image, 0, 0, null);
        graphics.dispose();
        return grayImage;
    }

    private static BufferedImage equalizeHistogram(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        int totalPixels = width * height;

        int[] histogram = new int[256];

        // Calculate histogram
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel = new Color(image.getRGB(x, y)).getRed();
                histogram[pixel]++;
            }
        }

        int[] cumulativeHistogram = new int[256];
        cumulativeHistogram[0] = histogram[0];

        // Calculate cumulative histogram
        for (int i = 1; i < 256; i++) {
            cumulativeHistogram[i] = cumulativeHistogram[i - 1] + histogram[i];
        }

        // Equalize image
        BufferedImage equalizedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int grayPixel = new Color(image.getRGB(x, y)).getRed();
                int equalizedGrayPixel = (int) (cumulativeHistogram[grayPixel] * 255.0 / totalPixels + 0.5);
                int equalizedRGB = (equalizedGrayPixel << 16) | (equalizedGrayPixel << 8) | equalizedGrayPixel;
                equalizedImage.setRGB(x, y, equalizedRGB);
            }
        }

        return equalizedImage;
    }

    private static void displayImage(BufferedImage image) {
        JFrame frame = new JFrame("Image Display");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        ImageIcon icon = new ImageIcon(image);


        JLabel label = new JLabel(icon);

        int width = icon.getIconWidth();
        int height = icon.getIconHeight();

  
        label.setPreferredSize(new Dimension(width, height));

 
        JScrollPane scrollPane = new JScrollPane(label);
        scrollPane.setPreferredSize(new Dimension(width, height));

        frame.getContentPane().add(scrollPane);

        frame.pack();
        frame.setVisible(true);
    }

    private static void saveImage(BufferedImage image, String filename) {
        try {
            File output = new File(filename);
            ImageIO.write(image, "jpg", output);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
