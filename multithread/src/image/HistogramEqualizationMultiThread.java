package image;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;
import javax.swing.*;

public class HistogramEqualizationMultiThread {

    public static void main(String[] args) {
        try {
            // Load image
            BufferedImage image = ImageIO.read(new File("Rain_Tree.jpg"));

            // Convert to grayscale
            BufferedImage grayImage = convertToGrayscale(image);

            // Equalize histogram using multi-threaded implementation
            int numOfThreads = 4; // Number of threads to use
            BufferedImage equalizedImage = equalizeHistogramMultiThread(grayImage, numOfThreads);

            // Display/save image
            displayImage(equalizedImage);
            saveImage(equalizedImage, "equalized_multi_thread.jpg");
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

    private static BufferedImage equalizeHistogramMultiThread(BufferedImage image, int numOfThreads) {
        int width = image.getWidth();
        int height = image.getHeight();
        int totalPixels = width * height;

        int[] histogram = new int[256];
        AtomicInteger[] sharedHistogram = new AtomicInteger[256];

        // Create shared histogram using atomic integers
        for (int i = 0; i < 256; i++) {
            sharedHistogram[i] = new AtomicInteger();
        }

        // Calculate histogram using multi-threading
        Thread[] threads = new Thread[numOfThreads];

        for (int i = 0; i < numOfThreads; i++) {
            final int threadIndex = i;

            threads[i] = new Thread(() -> {
                int startRow = (height / numOfThreads) * threadIndex;
                int endRow = (height / numOfThreads) * (threadIndex + 1);

                for (int y = startRow; y < endRow; y++) {
                    for (int x = 0; x < width; x++) {
                        int pixel = new Color(image.getRGB(x, y)).getRed();
                        sharedHistogram[pixel].getAndIncrement();
                    }
                }
            });

            threads[i].start();
        }

        // Wait for all threads to finish
        for (int i = 0; i < numOfThreads; i++) {
            try {
                threads[i].join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        // Calculate cumulative histogram using the shared histogram
        int[] cumulativeHistogram = new int[256];
        cumulativeHistogram[0] = sharedHistogram[0].get();

        for (int i = 1; i < 256; i++) {
            cumulativeHistogram[i] = cumulativeHistogram[i - 1] + sharedHistogram[i].get();
        }

        // Equalize image using multi-threading
        BufferedImage equalizedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        for (int i = 0; i < numOfThreads; i++) {
            final int threadIndex = i;

            threads[i] = new Thread(() -> {
                int startRow = (height / numOfThreads) * threadIndex;
                int endRow = (height / numOfThreads) * (threadIndex + 1);

                for (int y = startRow; y < endRow; y++) {
                    for (int x = 0; x < width; x++) {
                        int grayPixel = new Color(image.getRGB(x, y)).getRed();
                        int equalizedGrayPixel = (int) (cumulativeHistogram[grayPixel] * 255.0 / totalPixels + 0.5);
                        int equalizedRGB = (equalizedGrayPixel << 16) | (equalizedGrayPixel << 8) | equalizedGrayPixel;
                        equalizedImage.setRGB(x, y, equalizedRGB);
                    }
                }
            });

            threads[i].start();
        }

        // Wait for all threads to finish
        for (int i = 0; i < numOfThreads; i++) {
            try {
                threads[i].join();
            } catch (InterruptedException e) {
                e.printStackTrace();
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
