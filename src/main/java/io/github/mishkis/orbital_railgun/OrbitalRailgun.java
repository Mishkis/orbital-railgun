package io.github.mishkis.orbital_railgun;

import io.github.mishkis.orbital_railgun.item.OrbitalRailgunItem;
import io.github.mishkis.orbital_railgun.item.OrbitalRailgunItems;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.util.Identifier;

import java.util.logging.Logger;

public class OrbitalRailgun implements ModInitializer {
    public static final String MOD_ID = "orbital_railgun";
    public static final Logger LOGGER = Logger.getLogger(MOD_ID);

    public static final Identifier SHOOT_PACKET_ID = Identifier.of(MOD_ID, "shoot_packet");

    @Override
    public void onInitialize() {
        OrbitalRailgunItems.initialize();

        ServerPlayNetworking.registerGlobalReceiver(SHOOT_PACKET_ID, ((minecraftServer, serverPlayerEntity, serverPlayNetworkHandler, packetByteBuf, packetSender) -> {
            OrbitalRailgunItem orbitalRailgun = (OrbitalRailgunItem) packetByteBuf.readItemStack().getItem();

            minecraftServer.execute(() -> {
                orbitalRailgun.shoot(serverPlayerEntity);
            });
        }));
    }
}
