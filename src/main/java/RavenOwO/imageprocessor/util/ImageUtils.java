package RavenOwO.imageprocessor.util;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.*;
import java.util.stream.IntStream;

public class ImageUtils {

    public static BufferedImage toGrayscale(BufferedImage original) {
        int width = original.getWidth();
        int height = original.getHeight();
        BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);

        IntStream.range(0, height).parallel().forEach(y -> {
            for (int x = 0; x < width; x++) {
                int rgb = original.getRGB(x, y);
                result.setRGB(x, y, rgb);
            }
        });

        return result;
    }

    public static BufferedImage resize(BufferedImage original, int newWidth, int newHeight) {
        Image tmp = original.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);
        BufferedImage resized = new BufferedImage(newWidth, newHeight, original.getType());

        Graphics2D g2d = resized.createGraphics();
        g2d.drawImage(tmp, 0, 0, null);
        g2d.dispose();

        return resized;
    }

    public static BufferedImage rotate(BufferedImage original, double degrees) {
        int w = original.getWidth();
        int h = original.getHeight();

        BufferedImage rotated = new BufferedImage(w, h, original.getType());
        Graphics2D g2d = rotated.createGraphics();

        g2d.rotate(Math.toRadians(degrees), w / 2.0, h / 2.0);
        g2d.drawImage(original, 0, 0, null);
        g2d.dispose();

        return rotated;
    }

    public static BufferedImage flipHorizontal(BufferedImage original) {
        AffineTransform tx = AffineTransform.getScaleInstance(-1, 1);
        tx.translate(-original.getWidth(), 0);
        return transform(original, tx);
    }

    public static BufferedImage invertColors(BufferedImage original) {
        BufferedImage inverted = new BufferedImage(original.getWidth(), original.getHeight(), original.getType());

        for (int y = 0; y < original.getHeight(); y++) {
            for (int x = 0; x < original.getWidth(); x++) {
                int rgba = original.getRGB(x, y);
                Color col = new Color(rgba, true);
                Color newCol = new Color(255 - col.getRed(), 255 - col.getGreen(), 255 - col.getBlue());
                inverted.setRGB(x, y, newCol.getRGB());
            }
        }

        return inverted;
    }

    private static BufferedImage transform(BufferedImage original, AffineTransform tx) {
        AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
        return op.filter(original, null);
    }

    public static BufferedImage blur(BufferedImage image) {
        float[] matrix = {
                1/9f, 1/9f, 1/9f,
                1/9f, 1/9f, 1/9f,
                1/9f, 1/9f, 1/9f
        };
        return applyConvolution(image, matrix);
    }

    public static BufferedImage sharpen(BufferedImage image) {
        float[] matrix = {
                0, -1,  0,
                -1,  5, -1,
                0, -1,  0
        };
        return applyConvolution(image, matrix);
    }

    private static BufferedImage applyConvolution(BufferedImage image, float[] matrix) {
        BufferedImageOp op = new ConvolveOp(new Kernel(3, 3, matrix));
        return op.filter(image, null);
    }
}