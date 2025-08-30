package io.github.mishkis.orbital_railgun.client.rendering;

import io.github.mishkis.orbital_railgun.OrbitalRailgun;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.util.Identifier;
import org.joml.Vector3f;

public class OrbitalRailgunShader extends AbstractOrbitalRailgunShader {
    public static final Identifier ORBITAL_RAILGUN_SHADER = Identifier.of(OrbitalRailgun.MOD_ID, "shaders/post/orbital_railgun.json");
    public static final OrbitalRailgunShader INSTANCE = new OrbitalRailgunShader();

    public Vector3f BlockPosition = null;

    @Override
    protected Identifier getIdentifier() {
        return ORBITAL_RAILGUN_SHADER;
    }

    @Override
    protected boolean shouldRender() {
        return BlockPosition != null;
    }

    @Override
    public void onEndTick(MinecraftClient minecraftClient) {
        if (ticks >= 1400) {
            BlockPosition = null;
        }

        super.onEndTick(minecraftClient);
    }

    @Override
    public void onWorldRendered(Camera camera, float tickDelta, long nanoTime) {
        if (shouldRender()) {
            uniformBlockPosition.set(BlockPosition);
        }

        super.onWorldRendered(camera, tickDelta, nanoTime);
    }
}
