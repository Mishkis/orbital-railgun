package io.github.mishkis.orbital_railgun.client.mixin;

import io.github.mishkis.orbital_railgun.OrbitalRailgun;
import io.github.mishkis.orbital_railgun.client.rendering.OrbitalRailgunGuiShader;
import io.github.mishkis.orbital_railgun.client.rendering.OrbitalRailgunShader;
import io.github.mishkis.orbital_railgun.item.OrbitalRailgunItem;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.client.option.GameOptions;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Supplier;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {
    @Shadow @Final public GameOptions options;

    @Shadow @Nullable public ClientPlayerEntity player;

    @Shadow @Nullable public ClientPlayerInteractionManager interactionManager;

    @Inject(method = "handleInputEvents", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;isUsingItem()Z"))
    public void shootOnAttack(CallbackInfo ci) {
        if (player.getActiveItem().getItem() instanceof OrbitalRailgunItem orbitalRailgun && this.options.attackKey.isPressed() && OrbitalRailgunShader.INSTANCE.BlockPosition == null) {
            HitResult hitResult = OrbitalRailgunGuiShader.INSTANCE.hitResult;
            if (hitResult.getType() != HitResult.Type.MISS && hitResult instanceof BlockHitResult blockHitResult) {
                this.interactionManager.stopUsingItem(this.player);
                orbitalRailgun.shoot(this.player);
                OrbitalRailgunShader.INSTANCE.BlockPosition = blockHitResult.getBlockPos().toCenterPos().toVector3f();
                OrbitalRailgunShader.INSTANCE.Dimension = player.getWorld().getRegistryKey();

                PacketByteBuf buf = PacketByteBufs.create();
                buf.writeItemStack(orbitalRailgun.getDefaultStack());
                buf.writeBlockPos(blockHitResult.getBlockPos());

                ClientPlayNetworking.send(OrbitalRailgun.SHOOT_PACKET_ID, buf);
            }
        }
    }
}
