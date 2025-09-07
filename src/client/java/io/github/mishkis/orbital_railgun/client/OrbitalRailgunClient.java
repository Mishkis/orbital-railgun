package io.github.mishkis.orbital_railgun.client;

import io.github.mishkis.orbital_railgun.OrbitalRailgun;
import io.github.mishkis.orbital_railgun.client.item.OrbitalRailgunRenderer;
import io.github.mishkis.orbital_railgun.client.rendering.OrbitalRailgunGuiShader;
import io.github.mishkis.orbital_railgun.client.rendering.OrbitalRailgunShader;
import io.github.mishkis.orbital_railgun.item.OrbitalRailgunItems;
import ladysnake.satin.api.event.PostWorldRenderCallback;
import ladysnake.satin.api.event.ShaderEffectRenderCallback;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.render.item.BuiltinModelItemRenderer;
import net.minecraft.util.math.BlockPos;
import software.bernie.geckolib.animatable.client.RenderProvider;

public class OrbitalRailgunClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        OrbitalRailgunItems.ORBITAL_RAILGUN.renderProviderHolder.setValue(new RenderProvider() {
            private OrbitalRailgunRenderer renderer;

            @Override
            public BuiltinModelItemRenderer getCustomRenderer() {
                if (this.renderer == null) {
                    this.renderer = new OrbitalRailgunRenderer();
                }

                return this.renderer;
            }
        });

        ClientPlayNetworking.registerGlobalReceiver(OrbitalRailgun.CLIENT_SYNC_PACKET_ID, ((minecraftClient, clientPlayNetworkHandler, packetByteBuf, packetSender) -> {
            BlockPos blockPos = packetByteBuf.readBlockPos();

            minecraftClient.execute(() -> {
                OrbitalRailgunShader.INSTANCE.BlockPosition = blockPos.toCenterPos().toVector3f();
                OrbitalRailgunShader.INSTANCE.Dimension = minecraftClient.world.getRegistryKey();
            });
        }));

        ClientTickEvents.END_CLIENT_TICK.register(OrbitalRailgunGuiShader.INSTANCE);
        PostWorldRenderCallback.EVENT.register(OrbitalRailgunGuiShader.INSTANCE);

        ClientTickEvents.END_CLIENT_TICK.register(OrbitalRailgunShader.INSTANCE);
        PostWorldRenderCallback.EVENT.register(OrbitalRailgunShader.INSTANCE);
    }
}
