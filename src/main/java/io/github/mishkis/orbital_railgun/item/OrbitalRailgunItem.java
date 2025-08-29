package io.github.mishkis.orbital_railgun.item;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.item.Item;
import net.minecraft.util.Rarity;

public class OrbitalRailgunItem extends Item {
    public OrbitalRailgunItem() {
        super(new FabricItemSettings().rarity(Rarity.EPIC).maxCount(1));
    }
}
