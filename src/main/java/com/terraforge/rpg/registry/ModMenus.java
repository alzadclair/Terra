package com.terraforge.rpg.registry;

import com.terraforge.rpg.TerraForgeRPG;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Menu registry for TerraForge RPG.
 */
public final class ModMenus {
    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(Registries.MENU, TerraForgeRPG.MOD_ID);

    private ModMenus() {}
}
