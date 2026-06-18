package redegs.engine.graphics.pipelines;

import redegs.Engine;
import redegs.engine.engine.system.EntitySceneManager;
import redegs.engine.graphics.Texture;
import redegs.engine.graphics.buffers.FrameBuffer;
import redegs.engine.graphics.passes.*;
import redegs.engine.graphics.system.render.Pipeline;

public class DeferredPipeline extends Pipeline {
    private FrameBuffer gbuffer;
    private FrameBuffer shadowmap;

    private int gbuffer_id;

    public DeferredPipeline() {
        super();
    }

    @Override
    public void BuildPipeline() {
        super.BuildPipeline();
        gbuffer_id = EntitySceneManager.getInstance().createEntity();

        int height = Engine.getScreenHeight();
        int width = Engine.getScreenWidth();

        gbuffer = new FrameBuffer(width, height);
        gbuffer.addColorAttachment(new Texture(width, height, Texture.AttachmentType.POSITION, gbuffer_id));
        gbuffer.addColorAttachment(new Texture(width, height, Texture.AttachmentType.NORMALS, gbuffer_id));
        gbuffer.addColorAttachment(new Texture(width, height, Texture.AttachmentType.COLOR, gbuffer_id));
        gbuffer.addColorAttachment(new Texture(width, height, Texture.AttachmentType.COLOR, gbuffer_id));
        gbuffer.setDepthAttachment(new Texture(width, height, Texture.AttachmentType.DEPTH, gbuffer_id));
        gbuffer.completeAttachments();
        gbuffer.validate();

        shadowmap = new FrameBuffer(1024, 1024);
        shadowmap.setDepthAttachment(new Texture(1024, 1024, Texture.AttachmentType.DEPTH, gbuffer_id));
        shadowmap.completeAttachments();
        shadowmap.validate();

        getRenderContext().gbuffer = gbuffer;
        getRenderContext().shadowmap = shadowmap;


        NewPass(GeometryPass.class);
        NewPass(ShadowPass.class);
        NewPass(LightingPass.class);
        NewPass(SkyboxPass.class);
        NewPass(BillboardPass.class);
        NewPass(DebugPass.class);
        NewPass(BoundingBoxPass.class);
        NewPass(ImGuiPass.class);


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
