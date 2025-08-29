package io.github.mishkis.orbital_railgun.client;

import io.github.mishkis.orbital_railgun.OrbitalRailgun;
import io.github.mishkis.orbital_railgun.client.item.OrbitalRailgunRenderer;
import io.github.mishkis.orbital_railgun.item.OrbitalRailgunItems;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.render.item.BuiltinModelItemRenderer;
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
    }
}
