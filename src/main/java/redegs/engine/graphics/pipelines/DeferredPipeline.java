package redegs.engine.graphics.pipelines;

import redegs.Engine;
import redegs.engine.graphics.Texture;
import redegs.engine.graphics.buffers.FrameBuffer;
import redegs.engine.graphics.passes.GeometryPass;
import redegs.engine.graphics.passes.LightingPass;
import redegs.engine.graphics.passes.ShadowPass;
import redegs.engine.graphics.passes.SkyboxPass;
import redegs.engine.graphics.system.Pipeline;

public class DeferredPipeline extends Pipeline {
    private final FrameBuffer gbuffer;
    private final FrameBuffer shadowmap;

    public DeferredPipeline() {
        int width = Engine.getScreenWidth();
        int height = Engine.getScreenHeight();

        gbuffer = new FrameBuffer(width, height);
        gbuffer.addColorAttachment(new Texture(width, height, Texture.AttachmentType.POSITION));
        gbuffer.addColorAttachment(new Texture(width, height, Texture.AttachmentType.NORMALS));
        gbuffer.addColorAttachment(new Texture(width, height, Texture.AttachmentType.COLOR));
        gbuffer.addColorAttachment(new Texture(width, height, Texture.AttachmentType.COLOR));
        gbuffer.setDepthAttachment(new Texture(width, height, Texture.AttachmentType.DEPTH));
        gbuffer.completeAttachments();
        gbuffer.validate();

        shadowmap = new FrameBuffer(1024, 1024);
        shadowmap.setDepthAttachment(new Texture(1024, 1024, Texture.AttachmentType.DEPTH));
        shadowmap.completeAttachments();
        shadowmap.validate();

        getRenderContext().gbuffer = gbuffer;
        getRenderContext().shadowmap = shadowmap;


        NewPass(GeometryPass.class);
        NewPass(ShadowPass.class);
        NewPass(LightingPass.class);
        NewPass(SkyboxPass.class);

    }
//
//    @Override
//    protected void onPrePass(String render_pass_name) {
//        super.onPrePass(render_pass_name);
//
//        if (render_pass_name == "GeometryPass") {
//            gbuffer.bind();
//        }
//    }
//
//    @Override
//    protected void onPostPass(String render_pass_name) {
//        super.onPostPass(render_pass_name);
//
//        if (render_pass_name == "GeometryPass") {
//            gbuffer.unbind();
//        }
//    }
}
