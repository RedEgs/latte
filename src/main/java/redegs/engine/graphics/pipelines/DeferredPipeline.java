package redegs.engine.graphics.pipelines;

import redegs.Engine;
import redegs.engine.graphics.Pipeline;
import redegs.engine.graphics.Texture;
import redegs.engine.graphics.buffers.FrameBuffer;
import redegs.engine.graphics.passes.GeometryPass;
import redegs.engine.graphics.passes.LightingPass;

public class DeferredPipeline extends Pipeline {
    private final FrameBuffer gbuffer;

    public DeferredPipeline() {
        int width = Engine.getScreenWidth();
        int height = Engine.getScreenHeight();

        gbuffer = new FrameBuffer(width, height);
        gbuffer.addColorAttachment(new Texture(width, height, Texture.TextureType.POSITION));
        gbuffer.addColorAttachment(new Texture(width, height, Texture.TextureType.NORMALS));
        gbuffer.addColorAttachment(new Texture(width, height, Texture.TextureType.COLOR));
        gbuffer.setDepthAttachment(new Texture(width, height, Texture.TextureType.DEPTH));
        gbuffer.completeAttachments();
        gbuffer.validate();

        getRenderContext().gbuffer = gbuffer;

        NewPass(GeometryPass.class);
        NewPass(LightingPass.class);
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
