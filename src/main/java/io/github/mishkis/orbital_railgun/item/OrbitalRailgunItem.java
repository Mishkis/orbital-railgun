package io.github.mishkis.orbital_railgun.item;

import io.github.mishkis.orbital_railgun.OrbitalRailgun;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.item.Item;
import net.minecraft.util.Rarity;
import org.apache.commons.lang3.mutable.MutableObject;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.client.RenderProvider;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class OrbitalRailgunItem extends Item implements GeoItem {
    private final AnimatableInstanceCache CACHE = GeckoLibUtil.createInstanceCache(this);
    private final Supplier<Object> renderProvider = GeoItem.makeRenderer(this);
    public final MutableObject<RenderProvider> renderProviderHolder = new MutableObject<>();

    public OrbitalRailgunItem() {
        super(new FabricItemSettings().rarity(Rarity.EPIC).maxCount(1));
    }

    @Override
    public void createRenderer(Consumer<Object> consumer) {
        consumer.accept(this.renderProviderHolder.getValue());
    }

    @Override
    public Supplier<Object> getRenderProvider() {
        return renderProvider;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllerRegistrar) {}

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return CACHE;
    }
}
