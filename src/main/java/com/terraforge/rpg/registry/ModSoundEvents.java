package com.terraforge.rpg.registry;

import com.terraforge.rpg.TerraForgeRPG;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Sound registry for TerraForge RPG.
 */
public final class ModSoundEvents {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(Registries.SOUND_EVENT, TerraForgeRPG.MOD_ID);

    public static final DeferredHolder<SoundEvent, SoundEvent> LEVEL_UP =
            register("player.level_up");
    public static final DeferredHolder<SoundEvent, SoundEvent> ABILITY_ACTIVATE =
            register("ability.activate");
    public static final DeferredHolder<SoundEvent, SoundEvent> BOSS_ROAR =
            register("boss.roar");
    public static final DeferredHolder<SoundEvent, SoundEvent> EYE_AMBIENT =
            register("eye_of_cthulhu.ambient");
    public static final DeferredHolder<SoundEvent, SoundEvent> EYE_ROAR =
            register("eye_of_cthulhu.roar");
    public static final DeferredHolder<SoundEvent, SoundEvent> EYE_PREPARE =
            register("eye_of_cthulhu.prepare");
    public static final DeferredHolder<SoundEvent, SoundEvent> EYE_CHARGE =
            register("eye_of_cthulhu.charge");
    public static final DeferredHolder<SoundEvent, SoundEvent> EYE_BITE =
            register("eye_of_cthulhu.bite");
    public static final DeferredHolder<SoundEvent, SoundEvent> EYE_HURT =
            register("eye_of_cthulhu.hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> EYE_TRANSITION =
            register("eye_of_cthulhu.transition");
    public static final DeferredHolder<SoundEvent, SoundEvent> EYE_PHASE2_ROAR =
            register("eye_of_cthulhu.phase2_roar");
    public static final DeferredHolder<SoundEvent, SoundEvent> EYE_DEATH =
            register("eye_of_cthulhu.death");
    public static final DeferredHolder<SoundEvent, SoundEvent> EYE_SERVANT_SUMMON =
            register("eye_of_cthulhu.servant_summon");
    public static final DeferredHolder<SoundEvent, SoundEvent> ROAR_MECHANICAL =
            register("boss.roar_mechanical");
    public static final DeferredHolder<SoundEvent, SoundEvent> DEATHRAY =
            register("boss.deathray");
    public static final DeferredHolder<SoundEvent, SoundEvent> REFORGE_SUCCESS =
            register("item.reforge_success");
    public static final DeferredHolder<SoundEvent, SoundEvent> REFORGE_FAIL =
            register("item.reforge_fail");
    public static final DeferredHolder<SoundEvent, SoundEvent> MAGIC_MIRROR =
            register("item.magic_mirror");
    public static final DeferredHolder<SoundEvent, SoundEvent> HOOK_SHOOT =
            register("item.hook_shoot");
    public static final DeferredHolder<SoundEvent, SoundEvent> HOOK_LATCH =
            register("item.hook_latch");
    public static final DeferredHolder<SoundEvent, SoundEvent> MANA_RESTORE =
            register("player.mana_restore");
    public static final DeferredHolder<SoundEvent, SoundEvent> BEAM_SLASH =
            register("item.beam_slash");

    private static DeferredHolder<SoundEvent, SoundEvent> register(String name) {
        return SOUND_EVENTS.register(name, () -> SoundEvent.createVariableRangeEvent(
                ResourceLocation.fromNamespaceAndPath(TerraForgeRPG.MOD_ID, name)));
    }

    private ModSoundEvents() {}
}
