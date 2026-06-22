package redegs.engine.graphics;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Image {
    private ByteBuffer imageBuffer;
    public transient BufferedImage bufferedImage;

    public Image(String path_to_image) {
        Path path = Paths.get(path_to_image);
        try {
            bufferedImage = ImageIO.read(new File(path_to_image));
            imageBuffer = convertToRGBA(bufferedImage);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }



    }

    public Image(byte[] bytes) {
        fromBytes(bytes);
    }

    public Image(BufferedImage image) {
        bufferedImage = image;
        imageBuffer = convertToRGBA(image);
    }

    public ByteBuffer getByteBuffer() {
        return imageBuffer;
    }

    public static ByteBuffer convertToRGBA(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();

        // Create RGBA byte buffer (4 bytes per pixel)
        ByteBuffer buffer = ByteBuffer.allocateDirect(width * height * 4);
        buffer.order(ByteOrder.nativeOrder());

        // Ensure image is in ARGB format
        BufferedImage argbImage = image;
        if (image.getType() != BufferedImage.TYPE_INT_ARGB) {
            argbImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            argbImage.getGraphics().drawImage(image, 0, 0, null);
        }

        // Get pixel data
        int[] pixels = new int[width * height];
        argbImage.getRGB(0, 0, width, height, pixels, 0, width);

        // Convert ARGB to RGBA (OpenGL expects R,G,B,A order)
        for (int pixel : pixels) {
            buffer.put((byte) ((pixel >> 16) & 0xFF)); // R
            buffer.put((byte) ((pixel >> 8) & 0xFF));  // G
            buffer.put((byte) (pixel & 0xFF));         // B
            buffer.put((byte) ((pixel >> 24) & 0xFF)); // A
        }

        buffer.flip(); // Prepare buffer for reading
        return buffer;
    }

    public void fromBytes(byte[] bytes) {
        try {
            InputStream is = new ByteArrayInputStream(bytes);
            bufferedImage = ImageIO.read(is);
            imageBuffer = convertToRGBA(bufferedImage);
        } catch (Exception e) {
            throw new RuntimeException();
        }

    }

    public byte[] toBytes() {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(bufferedImage, "jpg", baos);
            byte[] bytes = baos.toByteArray();
            return bytes;
        } catch (Exception e) {
            throw new RuntimeException();
        }

    }

    public static byte[] toBytes(Image image) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image.bufferedImage, "jpg", baos);
            byte[] bytes = baos.toByteArray();
            return bytes;
        } catch (Exception e) {
            throw new RuntimeException();
        }
    }

    public static byte[] toBytes(BufferedImage bufferedImage) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(bufferedImage, "jpg", baos);
            byte[] bytes = baos.toByteArray();
            return bytes;
        } catch (Exception e) {
            throw new RuntimeException();
        }
    }

    public static byte[] empty(int width, int height) {
        BufferedImage blankImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        return toBytes(blankImage);
    }
}
