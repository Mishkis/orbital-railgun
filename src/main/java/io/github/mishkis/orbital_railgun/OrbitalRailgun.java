package io.github.mishkis.orbital_railgun;

import io.github.mishkis.orbital_railgun.item.OrbitalRailgunItem;
import io.github.mishkis.orbital_railgun.item.OrbitalRailgunItems;
import io.github.mishkis.orbital_railgun.util.OrbitalRailgunStrikeManager;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;

import java.util.List;
import java.util.logging.Logger;

public class OrbitalRailgun implements ModInitializer {
    public static final String MOD_ID = "orbital_railgun";
    public static final Logger LOGGER = Logger.getLogger(MOD_ID);

    public static final Identifier SHOOT_PACKET_ID = Identifier.of(MOD_ID, "shoot_packet");
    public static final Identifier CLIENT_SYNC_PACKET_ID = Identifier.of(MOD_ID, "client_synch_packet");

    @Override
    public void onInitialize() {
        OrbitalRailgunItems.initialize();
        OrbitalRailgunStrikeManager.initialize();

        ServerPlayNetworking.registerGlobalReceiver(SHOOT_PACKET_ID, ((minecraftServer, serverPlayerEntity, serverPlayNetworkHandler, packetByteBuf, packetSender) -> {
            OrbitalRailgunItem orbitalRailgun = (OrbitalRailgunItem) packetByteBuf.readItemStack().getItem();
            BlockPos blockPos = packetByteBuf.readBlockPos();

            minecraftServer.execute(() -> {
                orbitalRailgun.shoot(serverPlayerEntity);

                List<Entity> nearby = serverPlayerEntity.getWorld().getOtherEntities(null, Box.of(blockPos.toCenterPos(), 500., 500., 500.));
                OrbitalRailgunStrikeManager.activeStrikes.put(new Pair<>(blockPos, nearby), new Pair<>(minecraftServer.getTicks(), serverPlayerEntity.getWorld().getRegistryKey()));

                nearby.forEach((entity -> {
                    if (entity instanceof ServerPlayerEntity serverPlayer) {
                        ServerPlayNetworking.send(serverPlayer, CLIENT_SYNC_PACKET_ID, PacketByteBufs.create().writeBlockPos(blockPos));
                    }
                }));
            });
        }));

        ServerTickEvents.END_SERVER_TICK.register(OrbitalRailgunStrikeManager::tick);
    }
}
