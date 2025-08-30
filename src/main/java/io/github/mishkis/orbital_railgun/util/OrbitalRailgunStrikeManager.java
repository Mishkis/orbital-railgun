package io.github.mishkis.orbital_railgun.util;

import io.github.mishkis.orbital_railgun.OrbitalRailgun;
import net.minecraft.block.Blocks;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.joml.Vector2i;

import java.util.concurrent.ConcurrentHashMap;

public class OrbitalRailgunStrikeManager {
    public static ConcurrentHashMap<BlockPos, Pair<Integer, RegistryKey<World>>> activeStrikes = new ConcurrentHashMap<BlockPos, Pair<Integer, RegistryKey<World>>>();
    private static final int RADIUS = 24;
    private static final Boolean[][] mask = new Boolean[RADIUS * 2 + 1][RADIUS * 2 + 1];

    public static void tick(MinecraftServer server) {
        activeStrikes.forEach(((blockPos, keyPair) -> {
            if (server.getTicks() - keyPair.getLeft() >= 620) {
                activeStrikes.remove(blockPos);
                explode(blockPos, server.getWorld(keyPair.getRight()));
            }
        }));
    }

    private static void explode(BlockPos origin, World world) {
        for (int y = world.getBottomY(); y <= world.getHeight(); y++) {
            for (int x = -RADIUS; x <= RADIUS; x++) {
                for (int z = -RADIUS; z <= RADIUS; z++) {
                    if (mask[x + RADIUS][z + RADIUS]) {
                        world.setBlockState(new BlockPos(origin.getX() + x, y, origin.getZ() + z), Blocks.AIR.getDefaultState());
                    }
                }
            }
        }
    }

    public static void initialize() {
        for (int x = -RADIUS; x <= RADIUS; x++) {
            for (int z = -RADIUS; z <= RADIUS; z++) {
                mask[x + RADIUS][z + RADIUS] = Vector2i.lengthSquared(x, z) <= RADIUS * RADIUS;
            }
        }
    }
}
