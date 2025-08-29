package io.github.mishkis.orbital_railgun.client.rendering;

import io.github.mishkis.orbital_railgun.OrbitalRailgun;
import io.github.mishkis.orbital_railgun.item.OrbitalRailgunItem;
import ladysnake.satin.api.event.PostWorldRenderCallback;
import ladysnake.satin.api.event.ShaderEffectRenderCallback;
import ladysnake.satin.api.experimental.ReadableDepthFramebuffer;
import ladysnake.satin.api.managed.ManagedShaderEffect;
import ladysnake.satin.api.managed.ShaderEffectManager;
import ladysnake.satin.api.managed.uniform.Uniform1f;
import ladysnake.satin.api.managed.uniform.Uniform3f;
import ladysnake.satin.api.managed.uniform.UniformMat4;
import ladysnake.satin.api.util.GlMatrices;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public class OrbitalRailgunGuiShader implements PostWorldRenderCallback, ShaderEffectRenderCallback, ClientTickEvents.EndTick {
    public static final Identifier ORBITAL_RAILGUN_GUI_SHADER = Identifier.of(OrbitalRailgun.MOD_ID, "shaders/post/orbital_railgun_gui.json");
    public static final OrbitalRailgunGuiShader INSTANCE = new OrbitalRailgunGuiShader();

    private final MinecraftClient client = MinecraftClient.getInstance();

    private final Matrix4f projectionMatrix = new Matrix4f();

    final ManagedShaderEffect GUI_SHADER = ShaderEffectManager.getInstance().manage(ORBITAL_RAILGUN_GUI_SHADER, shader -> {
        shader.setSamplerUniform("DepthSampler", ((ReadableDepthFramebuffer)client.getFramebuffer()).getStillDepthMap());
    });
    private final UniformMat4 uniformInverseTransformMatrix = GUI_SHADER.findUniformMat4("InverseTransformMatrix");
    private final Uniform3f uniformCameraPosition = GUI_SHADER.findUniform3f("CameraPosition");
    private final Uniform1f uniformIsBlockHit = GUI_SHADER.findUniform1f("IsBlockHit");
    private final Uniform3f uniformBlockPosition = GUI_SHADER.findUniform3f("BlockPosition");
    private final Uniform1f uniformiTime = GUI_SHADER.findUniform1f("iTime");

    private int ticks = 0;

    private boolean shouldRender() {
        return client.player != null && client.player.getActiveItem().getItem() instanceof OrbitalRailgunItem;
    }

    @Override
    public void onEndTick(MinecraftClient minecraftClient) {
        if (shouldRender()) {
            ticks++;
        } else {
            ticks = 0;
        }
    }

    @Override
    public void onWorldRendered(Camera camera, float tickDelta, long nanoTime) {
        if (shouldRender()) {
            uniformInverseTransformMatrix.set(GlMatrices.getInverseTransformMatrix(projectionMatrix));
            uniformCameraPosition.set(camera.getPos().toVector3f());

            HitResult hitResult = client.player.raycast(100f, tickDelta, false);
            switch (hitResult.getType()) {
                case BLOCK:
                    uniformIsBlockHit.set(1);
                    uniformBlockPosition.set(((BlockHitResult) hitResult).getBlockPos().toCenterPos().toVector3f());
                    break;
                case ENTITY:
                    uniformIsBlockHit.set(1);
                    uniformBlockPosition.set(((EntityHitResult) hitResult).getEntity().getBlockPos().toCenterPos().toVector3f());
                    break;
                case MISS:
                    uniformIsBlockHit.set(0);
                    break;
            }

            uniformiTime.set((ticks + tickDelta)/20f);
        }
    }

    @Override
    public void renderShaderEffects(float tickDelta) {
        if (shouldRender()) {
            GUI_SHADER.render(tickDelta);
        }
    }
}
