package io.github.jasminecrash.looksmaxxing.client.render;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.*;
import com.mojang.blaze3d.platform.CompareOp;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import io.github.jasminecrash.looksmaxxing.utils.Frustum;
import io.github.jasminecrash.looksmaxxing.utils.MathUtils;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MappableRingBuffer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.resources.Identifier;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryUtil;

import java.util.OptionalDouble;
import java.util.OptionalInt;

import static io.github.jasminecrash.looksmaxxing.Looksmaxxing.MOD_ID;

public class FrustumRenderer {
    private static final float CR = 0.6f, CG = 0.0f, CB = 1.0f, CA = 0.2f;

    private static final RenderPipeline FRUSTUM_PIPELINE = RenderPipelines.register(
            RenderPipeline.builder(RenderPipelines.DEBUG_FILLED_SNIPPET)
                    .withLocation(Identifier.fromNamespaceAndPath(MOD_ID, "pipeline/frustum_view"))
                    .withDepthStencilState(new DepthStencilState(CompareOp.LESS_THAN_OR_EQUAL, false))
                    .withVertexFormat(RenderPipelines.DEBUG_FILLED_SNIPPET.vertexFormat().get(), VertexFormat.Mode.QUADS)
                    .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
                    .withCull(false)
                    .build()
    );

    private static final ByteBufferBuilder allocator = new ByteBufferBuilder(RenderType.SMALL_BUFFER_SIZE);
    private BufferBuilder buffer;
    private MappableRingBuffer vertexBuffer;

    private static final Vector4f COLOR_MODULATOR = new Vector4f(1f, 1f, 1f, 1f);
    private static final Vector3f MODEL_OFFSET    = new Vector3f();
    private static final Matrix4f TEXTURE_MATRIX  = new Matrix4f();

    private static FrustumRenderer instance = null;
    public FrustumRenderer() {
        if (instance != null) { return; }
        instance = this;
        LevelRenderEvents.AFTER_TRANSLUCENT_TERRAIN.register(this::onLevelRender);
    }
    public static FrustumRenderer getInstance() { return instance; }



    public void onLevelRender(LevelRenderContext context) {
        Minecraft mc = Minecraft.getInstance();
        ClientLevel level = mc.level;
        if (level == null) { return; }

        // Extract (write vertices)
//        for(AbstractClientPlayer player : level.players().stream()
//                .filter(person -> person.getAttachedOrElse(ModDataAttachments.CASTING_THE_LOOK, false)).toList()) {
//            extractCone(context, player);
//        }
        //extractFrustum(context, mc.player);

        // Draw (upload + dispatch)
        if (buffer != null) {
            drawFrustum(mc);
        }
    }

    private void extractFrustum(LevelRenderContext context, AbstractClientPlayer player) {

        PoseStack matrices = context.poseStack();
        CameraRenderState cameraState = context.levelState().cameraRenderState;
        matrices.pushPose();
        matrices.translate(-cameraState.pos.x, -cameraState.pos.y, -cameraState.pos.z); //shift into world space

        Minecraft mc = Minecraft.getInstance();
        float tickDelta = mc.getDeltaTracker().getGameTimeDeltaPartialTick(true);
        Vector3f look = (player == mc.player ? player.getLookAngle() : player.getHeadLookAngle()).toVector3f();
        Vector3f apex;
        if (mc.options.getCameraType() == CameraType.FIRST_PERSON && player == mc.player) {
            apex = cameraState.pos.toVector3f().sub(look.mul(0.5f));
        } else {
            apex = player.getEyePosition(tickDelta).toVector3f();
        }

        Frustum visualFrustum = new Frustum(
                apex,
                look,
                player.getUpVector(tickDelta).toVector3f(),
                (float)Math.toRadians(90),
                (float)Math.toRadians(90),
                0.5f,
                16.0f
        );

        Vector3f[] surface = MathUtils.generateFrustumSurface(visualFrustum);
        Matrix4fc pose = matrices.last().pose();

        if (buffer == null) {
            buffer = new BufferBuilder(allocator, FRUSTUM_PIPELINE.getVertexFormatMode(), FRUSTUM_PIPELINE.getVertexFormat());
        }

        //chop off the near and far clip planes for now by starting a bit ahead
        for (Vector3f vector3f : surface) {
            buffer.addVertex(pose, vector3f.x, vector3f.y, vector3f.z).setColor(CR, CG, CB, CA);
        }

        matrices.popPose();
    }

    private GpuBuffer uploadVertices(MeshData.DrawState dp, VertexFormat fmt, MeshData mesh) {
        int size = dp.vertexCount() * fmt.getVertexSize();

        if (vertexBuffer == null || vertexBuffer.size() < size) {
            if (vertexBuffer != null) vertexBuffer.close();
            vertexBuffer = new MappableRingBuffer(
                    () -> MOD_ID + " frustum view vbo",
                    GpuBuffer.USAGE_VERTEX | GpuBuffer.USAGE_MAP_WRITE,
                    size
            );
        }

        CommandEncoder enc = RenderSystem.getDevice().createCommandEncoder();
        try (GpuBuffer.MappedView view = enc.mapBuffer(
                vertexBuffer.currentBuffer().slice(0, mesh.vertexBuffer().remaining()),
                false, true)) {
            MemoryUtil.memCopy(mesh.vertexBuffer(), view.data());
        }
        return vertexBuffer.currentBuffer();
    }

    private void drawMesh(Minecraft mc, MeshData mesh, MeshData.DrawState dp, GpuBuffer vbo) {
        mesh.sortQuads(allocator, RenderSystem.getProjectionType().vertexSorting());
        GpuBuffer indices = FRUSTUM_PIPELINE.getVertexFormat().uploadImmediateIndexBuffer(mesh.indexBuffer());
        VertexFormat.IndexType indexType = mesh.drawState().indexType();

        GpuBufferSlice dynamicTransforms = RenderSystem.getDynamicUniforms()
                .writeTransform(
                        RenderSystem.getModelViewMatrix(),
                        COLOR_MODULATOR,
                        MODEL_OFFSET,
                        TEXTURE_MATRIX
                );

        RenderTarget main = mc.getMainRenderTarget();
        RenderTarget translucent = mc.levelRenderer.getTranslucentTarget();
        RenderTarget target = main;
        if(mc.options.improvedTransparency().get() && translucent != null) { target = translucent; }

        try (RenderPass pass = RenderSystem.getDevice()
                .createCommandEncoder()
                .createRenderPass(
                        () -> MOD_ID + " frustum view pass",
                        target.getColorTextureView(),
                        OptionalInt.empty(),
                        target.getDepthTextureView(),
                        OptionalDouble.empty())) {

            pass.setPipeline(FRUSTUM_PIPELINE);
            RenderSystem.bindDefaultUniforms(pass);
            pass.setUniform("DynamicTransforms", dynamicTransforms);
            pass.setVertexBuffer(0, vbo);
            pass.setIndexBuffer(indices, indexType);
            pass.drawIndexed(0, 0, dp.indexCount(), 1);
        }

        mesh.close();
    }

    private void drawFrustum(Minecraft mc) {
        MeshData mesh = buffer.buildOrThrow();
        MeshData.DrawState dp = mesh.drawState();
        VertexFormat fmt = dp.format();

        GpuBuffer vbo = uploadVertices(dp, fmt, mesh);
        drawMesh(mc, mesh, dp, vbo);

        vertexBuffer.rotate();
        buffer = null;
    }

    public void close() {
        allocator.close();
        if (vertexBuffer != null) {
            vertexBuffer.close();
            vertexBuffer = null;
        }
    }
}
