package redegs.engine.graphics.buffers;

import org.lwjgl.BufferUtils;
import redegs.Engine;
import redegs.engine.graphics.Texture;

import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL30C.*;

public class FrameBuffer {
    private final int id;

    private int width, height;

    private final List<Texture> attachments;
    private Texture depth_texture;

    private boolean locked = false;

    public FrameBuffer(int width, int height) {
        id = glGenFramebuffers();

        this.width = width;
        this.height = height;

        attachments = new ArrayList<>();
    }

    public void bind() {
        glBindFramebuffer(GL_FRAMEBUFFER, id);
        glViewport(0, 0, width, height);
    }
    public void unbind() {
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
    }
    public void resize(int width, int height) {
        for (int i = 0; i < attachments.size(); i++) {
            Texture t = attachments.get(i);
            t.resize(width, height);
        }
        depth_texture.resize(width, height);
    }


    public void addColorAttachment(Texture texture) {
        bind();

        int index = attachments.size();
        System.err.println(index);
        glFramebufferTexture2D(
                GL_FRAMEBUFFER,
                GL_COLOR_ATTACHMENT0 + index,
                GL_TEXTURE_2D,
                texture.getId(),
                0
        );

        attachments.add(texture);

        unbind();
    }
    public void setDepthAttachment(Texture texture) {
        bind();

        glFramebufferTexture2D(
                GL_FRAMEBUFFER,
                GL_DEPTH_ATTACHMENT,
                GL_TEXTURE_2D,
                texture.getId(),
                0
        );

        depth_texture = texture;

        unbind();
    }
    public void completeAttachments() {
        bind();
        IntBuffer buffers = BufferUtils.createIntBuffer(attachments.size());
        for (int i = 0; i < attachments.size(); i++) {
            buffers.put(GL_COLOR_ATTACHMENT0 + i);
            System.err.println(i);
        }
        buffers.flip();
        glDrawBuffers(buffers);
        unbind();
    }
    public void blitToDefaultFramebuffer() {
        bind();

        glBindFramebuffer(GL_READ_FRAMEBUFFER, id);
        glBindFramebuffer(GL_DRAW_FRAMEBUFFER, 0); // write to default framebuffer
        glBlitFramebuffer(
                0, 0, Engine.getScreenWidth(), Engine.getScreenHeight(), 0, 0, Engine.getScreenWidth(), Engine.getScreenHeight(), GL_DEPTH_BUFFER_BIT, GL_NEAREST
        );

        unbind();
    }



    public void validate() {
        bind();

        if (glCheckFramebufferStatus(GL_FRAMEBUFFER)
                != GL_FRAMEBUFFER_COMPLETE) {
            throw new RuntimeException("Framebuffer incomplete");
        }

        unbind();
    }

    public List<Texture> getColorTextures() {
        return this.attachments;
    }

    public Texture getColorTexture(int i) {
        return this.attachments.get(i);
    }

    public Texture getDepthTexture() {
        return this.depth_texture;
    }

    public void destroy() {
        glDeleteFramebuffers(id);
    }
}
