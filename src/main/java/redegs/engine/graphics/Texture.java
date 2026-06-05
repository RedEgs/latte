package redegs.engine.graphics;

import redegs.Engine;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import static org.lwjgl.opengl.GL11C.*;
import static org.lwjgl.opengl.GL13C.*;
import static org.lwjgl.opengl.GL30C.glGenerateMipmap;

public class Texture {
    private int id;
    private Image image;

    public Texture(String path_to_texture) {
        id = glGenTextures();
        image = new Image(path_to_texture);
        ByteBuffer bytes = image.getByteBuffer();
        bytes.flip();

        glBindTexture(GL_TEXTURE_2D, id);
        glPixelStorei(GL_UNPACK_ALIGNMENT, 1);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, image.bufferedImage.getWidth(), image.bufferedImage.getHeight(), 0, GL_RGBA, GL_UNSIGNED_BYTE, bytes);
        glGenerateMipmap(GL_TEXTURE_2D);

    }

    public void bind() {
        glBindTexture(GL_TEXTURE_2D, id);
    }

    public void use(Shader shader, int texureUnit) {
        glActiveTexture(GL_TEXTURE0+texureUnit);

        glBindTexture(GL_TEXTURE_2D, id);
        shader.setUniform1i("texture0", texureUnit);
    }


}
