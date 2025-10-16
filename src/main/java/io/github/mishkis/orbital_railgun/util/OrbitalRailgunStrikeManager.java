package io.github.mishkis.orbital_railgun.util;

import io.github.mishkis.orbital_railgun.OrbitalRailgun;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.joml.Vector2i;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class OrbitalRailgunStrikeManager {
    public static ConcurrentHashMap<Pair<BlockPos, List<Entity>>, Pair<Integer, RegistryKey<World>>> activeStrikes = new ConcurrentHashMap<Pair<BlockPos, List<Entity>>, Pair<Integer, RegistryKey<World>>>();
    private static final RegistryKey<DamageType> STRIKE_DAMAGE = RegistryKey.of(RegistryKeys.DAMAGE_TYPE, Identifier.of(OrbitalRailgun.MOD_ID, "strike"));
    private static final int RADIUS = 24;
    private static final int RADIUS_SQUARED = RADIUS * RADIUS;
    private static final Boolean[][] mask = new Boolean[RADIUS * 2 + 1][RADIUS * 2 + 1];

    public static void tick(MinecraftServer server) {
        activeStrikes.forEach(((keyPair1, keyPair2) -> {
            float age = server.getTicks() - keyPair2.getLeft();
            BlockPos blockPos = keyPair1.getLeft();
            List<Entity> entities = keyPair1.getRight();
            RegistryKey<World> dimension = keyPair2.getRight();
            if (age >= 700) {
                activeStrikes.remove(keyPair1);

                ServerWorld world = server.getWorld(dimension);

                entities.forEach(entity -> {
                    if (entity.getWorld().getRegistryKey() == dimension && entity.getPos().subtract(blockPos.toCenterPos()).lengthSquared() <= RADIUS_SQUARED) {
                        entity.damage(new DamageSource(world.getRegistryManager().get(RegistryKeys.DAMAGE_TYPE).getEntry(STRIKE_DAMAGE).get()), 100000f);
                    }
                });

                explode(blockPos, world);
            } else if (age >= 400) {
                entities.forEach(entity -> {
                    if (entity instanceof PlayerEntity player && player.isSpectator()) {
                        return;
                    }
                    if (entity.getWorld().getRegistryKey() == dimension) {
                        Vec3d dir = blockPos.toCenterPos().subtract(entity.getPos());
                        double mag = Math.min(1. / Math.abs(dir.length() - 20.) * 4. * (age - 400.) / 300., 5.);
                        dir = dir.normalize();

                        entity.addVelocity(dir.multiply(mag));
                        entity.velocityModified = true;
                    }
                });
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
                mask[x + RADIUS][z + RADIUS] = Vector2i.lengthSquared(x, z) <= RADIUS_SQUARED;
            }
        }
    }
}
