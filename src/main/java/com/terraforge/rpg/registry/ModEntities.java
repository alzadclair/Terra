package com.terraforge.rpg.registry;

import com.terraforge.rpg.TerraForgeRPG;
import com.terraforge.rpg.boss.prehardmode.EyeOfCthulhuEntity;
import com.terraforge.rpg.entity.mob.DemonEyeEntity;
import com.terraforge.rpg.entity.mob.ServantOfCthulhuEntity;
import com.terraforge.rpg.entity.mob.TerraSlimeEntity;
import com.terraforge.rpg.entity.mob.TerraZombieEntity;
import com.terraforge.rpg.entity.projectile.TerraProjectileEntity;
import com.terraforge.rpg.entity.projectile.YoyoEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Entity registry for TerraForge RPG.
 */
public final class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(Registries.ENTITY_TYPE, TerraForgeRPG.MOD_ID);

    // Projectiles & Tethered Entities
    public static final DeferredHolder<EntityType<?>, EntityType<TerraProjectileEntity>> TERRA_PROJECTILE =
            ENTITIES.register("terra_projectile", () -> EntityType.Builder.<TerraProjectileEntity>of(
                    TerraProjectileEntity::new, MobCategory.MISC)
                    .sized(0.5f, 0.5f)
                    .clientTrackingRange(64)
                    .updateInterval(1)
                    .build("terra_projectile"));

    public static final DeferredHolder<EntityType<?>, EntityType<YoyoEntity>> YOYO =
            ENTITIES.register("yoyo", () -> EntityType.Builder.<YoyoEntity>of(
                    YoyoEntity::new, MobCategory.MISC)
                    .sized(0.4f, 0.4f)
                    .clientTrackingRange(64)
                    .updateInterval(1)
                    .build("yoyo"));

    public static final DeferredHolder<EntityType<?>, EntityType<com.terraforge.rpg.entity.projectile.GrapplingHookEntity>> GRAPPLING_HOOK =
            ENTITIES.register("grappling_hook", () -> EntityType.Builder.<com.terraforge.rpg.entity.projectile.GrapplingHookEntity>of(
                    com.terraforge.rpg.entity.projectile.GrapplingHookEntity::new, MobCategory.MISC)
                    .sized(0.3f, 0.3f)
                    .clientTrackingRange(64)
                    .updateInterval(1)
                    .build("grappling_hook"));

    // Terraria Pre-Hardmode Mobs
    public static final DeferredHolder<EntityType<?>, EntityType<TerraSlimeEntity>> GREEN_SLIME =
            ENTITIES.register("green_slime", () -> EntityType.Builder.<TerraSlimeEntity>of(
                    (type, level) -> new TerraSlimeEntity(type, level, TerraSlimeEntity.SlimeVariant.GREEN),
                    MobCategory.MONSTER)
                    .sized(0.8f, 0.8f)
                    .clientTrackingRange(48)
                    .updateInterval(2)
                    .build("green_slime"));

    public static final DeferredHolder<EntityType<?>, EntityType<TerraSlimeEntity>> BLUE_SLIME =
            ENTITIES.register("blue_slime", () -> EntityType.Builder.<TerraSlimeEntity>of(
                    (type, level) -> new TerraSlimeEntity(type, level, TerraSlimeEntity.SlimeVariant.BLUE),
                    MobCategory.MONSTER)
                    .sized(1.0f, 1.0f)
                    .clientTrackingRange(48)
                    .updateInterval(2)
                    .build("blue_slime"));

    public static final DeferredHolder<EntityType<?>, EntityType<DemonEyeEntity>> DEMON_EYE =
            ENTITIES.register("demon_eye", () -> EntityType.Builder.<DemonEyeEntity>of(
                    DemonEyeEntity::new, MobCategory.MONSTER)
                    .sized(0.9f, 0.9f)
                    .clientTrackingRange(64)
                    .updateInterval(1)
                    .build("demon_eye"));

    public static final DeferredHolder<EntityType<?>, EntityType<TerraZombieEntity>> TERRA_ZOMBIE =
            ENTITIES.register("terra_zombie", () -> EntityType.Builder.<TerraZombieEntity>of(
                    TerraZombieEntity::new, MobCategory.MONSTER)
                    .sized(0.6f, 1.95f)
                    .clientTrackingRange(48)
                    .updateInterval(2)
                    .build("terra_zombie"));

    public static final DeferredHolder<EntityType<?>, EntityType<com.terraforge.rpg.entity.mob.FaceMonsterEntity>> FACE_MONSTER =
            ENTITIES.register("face_monster", () -> EntityType.Builder.<com.terraforge.rpg.entity.mob.FaceMonsterEntity>of(
                    com.terraforge.rpg.entity.mob.FaceMonsterEntity::new, MobCategory.MONSTER)
                    .sized(0.7f, 2.2f)
                    .clientTrackingRange(64)
                    .updateInterval(2)
                    .build("face_monster"));

    public static final DeferredHolder<EntityType<?>, EntityType<EyeOfCthulhuEntity>> EYE_OF_CTHULHU =
            ENTITIES.register("eye_of_cthulhu", () -> EntityType.Builder.<EyeOfCthulhuEntity>of(
                    EyeOfCthulhuEntity::new, MobCategory.MONSTER)
                    .sized(2.5f, 2.5f)
                    .clientTrackingRange(96)
                    .updateInterval(1)
                    .build("eye_of_cthulhu"));

    public static final DeferredHolder<EntityType<?>, EntityType<ServantOfCthulhuEntity>> SERVANT_OF_CTHULHU =
            ENTITIES.register("servant_of_cthulhu", () -> EntityType.Builder.<ServantOfCthulhuEntity>of(
                    ServantOfCthulhuEntity::new, MobCategory.MONSTER)
                    .sized(0.5f, 0.5f)
                    .clientTrackingRange(48)
                    .updateInterval(1)
                    .build("servant_of_cthulhu"));

    public static final DeferredHolder<EntityType<?>, EntityType<com.terraforge.rpg.boss.prehardmode.WallOfFleshEntity>> WALL_OF_FLESH =
            ENTITIES.register("wall_of_flesh", () -> EntityType.Builder.<com.terraforge.rpg.boss.prehardmode.WallOfFleshEntity>of(
                    com.terraforge.rpg.boss.prehardmode.WallOfFleshEntity::new, MobCategory.MONSTER)
                    .sized(4.0f, 12.0f)
                    .clientTrackingRange(128)
                    .updateInterval(1)
                    .build("wall_of_flesh"));

    public static final DeferredHolder<EntityType<?>, EntityType<com.terraforge.rpg.boss.prehardmode.TheHungryEntity>> THE_HUNGRY =
            ENTITIES.register("the_hungry", () -> EntityType.Builder.<com.terraforge.rpg.boss.prehardmode.TheHungryEntity>of(
                    com.terraforge.rpg.boss.prehardmode.TheHungryEntity::new, MobCategory.MONSTER)
                    .sized(1.0f, 1.0f)
                    .clientTrackingRange(64)
                    .updateInterval(1)
                    .build("the_hungry"));

    public static final DeferredHolder<EntityType<?>, EntityType<com.terraforge.rpg.boss.prehardmode.KingSlimeEntity>> KING_SLIME =
            ENTITIES.register("king_slime", () -> EntityType.Builder.<com.terraforge.rpg.boss.prehardmode.KingSlimeEntity>of(
                    com.terraforge.rpg.boss.prehardmode.KingSlimeEntity::new, MobCategory.MONSTER)
                    .sized(3.5f, 3.5f)
                    .clientTrackingRange(128)
                    .updateInterval(1)
                    .build("king_slime"));

    public static final DeferredHolder<EntityType<?>, EntityType<com.terraforge.rpg.entity.mob.goblin.GoblinPeonEntity>> GOBLIN_PEON =
            ENTITIES.register("goblin_peon", () -> EntityType.Builder.<com.terraforge.rpg.entity.mob.goblin.GoblinPeonEntity>of(
                    com.terraforge.rpg.entity.mob.goblin.GoblinPeonEntity::new, MobCategory.MONSTER)
                    .sized(0.7f, 1.5f)
                    .clientTrackingRange(48)
                    .updateInterval(2)
                    .build("goblin_peon"));

    public static final DeferredHolder<EntityType<?>, EntityType<com.terraforge.rpg.entity.mob.goblin.GoblinThiefEntity>> GOBLIN_THIEF =
            ENTITIES.register("goblin_thief", () -> EntityType.Builder.<com.terraforge.rpg.entity.mob.goblin.GoblinThiefEntity>of(
                    com.terraforge.rpg.entity.mob.goblin.GoblinThiefEntity::new, MobCategory.MONSTER)
                    .sized(0.7f, 1.5f)
                    .clientTrackingRange(48)
                    .updateInterval(2)
                    .build("goblin_thief"));

    public static final DeferredHolder<EntityType<?>, EntityType<com.terraforge.rpg.entity.mob.goblin.GoblinWarriorEntity>> GOBLIN_WARRIOR =
            ENTITIES.register("goblin_warrior", () -> EntityType.Builder.<com.terraforge.rpg.entity.mob.goblin.GoblinWarriorEntity>of(
                    com.terraforge.rpg.entity.mob.goblin.GoblinWarriorEntity::new, MobCategory.MONSTER)
                    .sized(0.8f, 1.8f)
                    .clientTrackingRange(48)
                    .updateInterval(2)
                    .build("goblin_warrior"));

    public static final DeferredHolder<EntityType<?>, EntityType<com.terraforge.rpg.entity.mob.goblin.GoblinSorcererEntity>> GOBLIN_SORCERER =
            ENTITIES.register("goblin_sorcerer", () -> EntityType.Builder.<com.terraforge.rpg.entity.mob.goblin.GoblinSorcererEntity>of(
                    com.terraforge.rpg.entity.mob.goblin.GoblinSorcererEntity::new, MobCategory.MONSTER)
                    .sized(0.7f, 1.6f)
                    .clientTrackingRange(48)
                    .updateInterval(2)
                    .build("goblin_sorcerer"));

    // Town NPCs
    public static final DeferredHolder<EntityType<?>, EntityType<com.terraforge.rpg.entity.npc.GuideEntity>> GUIDE =
            ENTITIES.register("guide", () -> EntityType.Builder.<com.terraforge.rpg.entity.npc.GuideEntity>of(
                    com.terraforge.rpg.entity.npc.GuideEntity::new, MobCategory.CREATURE)
                    .sized(0.6f, 1.95f)
                    .clientTrackingRange(64)
                    .updateInterval(2)
                    .build("guide"));

    public static final DeferredHolder<EntityType<?>, EntityType<com.terraforge.rpg.entity.npc.MerchantEntity>> MERCHANT =
            ENTITIES.register("merchant", () -> EntityType.Builder.<com.terraforge.rpg.entity.npc.MerchantEntity>of(
                    com.terraforge.rpg.entity.npc.MerchantEntity::new, MobCategory.CREATURE)
                    .sized(0.6f, 1.95f)
                    .clientTrackingRange(64)
                    .updateInterval(2)
                    .build("merchant"));

    public static final DeferredHolder<EntityType<?>, EntityType<com.terraforge.rpg.entity.npc.NurseEntity>> NURSE =
            ENTITIES.register("nurse", () -> EntityType.Builder.<com.terraforge.rpg.entity.npc.NurseEntity>of(
                    com.terraforge.rpg.entity.npc.NurseEntity::new, MobCategory.CREATURE)
                    .sized(0.6f, 1.95f)
                    .clientTrackingRange(64)
                    .updateInterval(2)
                    .build("nurse"));

    public static final DeferredHolder<EntityType<?>, EntityType<com.terraforge.rpg.entity.npc.GoblinTinkererEntity>> GOBLIN_TINKERER =
            ENTITIES.register("goblin_tinkerer", () -> EntityType.Builder.<com.terraforge.rpg.entity.npc.GoblinTinkererEntity>of(
                    com.terraforge.rpg.entity.npc.GoblinTinkererEntity::new, MobCategory.CREATURE)
                    .sized(0.6f, 1.7f)
                    .clientTrackingRange(64)
                    .updateInterval(2)
                    .build("goblin_tinkerer"));

    // Mounts
    public static final DeferredHolder<EntityType<?>, EntityType<com.terraforge.rpg.entity.mount.SlimeMountEntity>> SLIME_MOUNT =
            ENTITIES.register("slime_mount", () -> EntityType.Builder.<com.terraforge.rpg.entity.mount.SlimeMountEntity>of(
                    com.terraforge.rpg.entity.mount.SlimeMountEntity::new, MobCategory.CREATURE)
                    .sized(1.2f, 1.2f)
                    .clientTrackingRange(64)
                    .updateInterval(1)
                    .build("slime_mount"));

    // Hardmode Mechanical Bosses & Mobs
    public static final DeferredHolder<EntityType<?>, EntityType<com.terraforge.rpg.boss.hardmode.RetinazerEntity>> RETINAZER =
            ENTITIES.register("retinazer", () -> EntityType.Builder.<com.terraforge.rpg.boss.hardmode.RetinazerEntity>of(
                    com.terraforge.rpg.boss.hardmode.RetinazerEntity::new, MobCategory.MONSTER)
                    .sized(2.8f, 2.8f)
                    .clientTrackingRange(96)
                    .updateInterval(1)
                    .build("retinazer"));

    public static final DeferredHolder<EntityType<?>, EntityType<com.terraforge.rpg.boss.hardmode.SpazmatismEntity>> SPAZMATISM =
            ENTITIES.register("spazmatism", () -> EntityType.Builder.<com.terraforge.rpg.boss.hardmode.SpazmatismEntity>of(
                    com.terraforge.rpg.boss.hardmode.SpazmatismEntity::new, MobCategory.MONSTER)
                    .sized(2.8f, 2.8f)
                    .clientTrackingRange(96)
                    .updateInterval(1)
                    .build("spazmatism"));

    public static final DeferredHolder<EntityType<?>, EntityType<com.terraforge.rpg.boss.hardmode.DestroyerProbeEntity>> DESTROYER_PROBE =
            ENTITIES.register("destroyer_probe", () -> EntityType.Builder.<com.terraforge.rpg.boss.hardmode.DestroyerProbeEntity>of(
                    com.terraforge.rpg.boss.hardmode.DestroyerProbeEntity::new, MobCategory.MONSTER)
                    .sized(0.8f, 0.8f)
                    .clientTrackingRange(64)
                    .updateInterval(1)
                    .build("destroyer_probe"));

    public static final DeferredHolder<EntityType<?>, EntityType<com.terraforge.rpg.boss.hardmode.TheDestroyerEntity>> THE_DESTROYER =
            ENTITIES.register("the_destroyer", () -> EntityType.Builder.<com.terraforge.rpg.boss.hardmode.TheDestroyerEntity>of(
                    com.terraforge.rpg.boss.hardmode.TheDestroyerEntity::new, MobCategory.MONSTER)
                    .sized(3.0f, 3.0f)
                    .clientTrackingRange(128)
                    .updateInterval(1)
                    .build("the_destroyer"));

    public static final DeferredHolder<EntityType<?>, EntityType<com.terraforge.rpg.boss.hardmode.SkeletronPrimeEntity>> SKELETRON_PRIME =
            ENTITIES.register("skeletron_prime", () -> EntityType.Builder.<com.terraforge.rpg.boss.hardmode.SkeletronPrimeEntity>of(
                    com.terraforge.rpg.boss.hardmode.SkeletronPrimeEntity::new, MobCategory.MONSTER)
                    .sized(3.2f, 3.2f)
                    .clientTrackingRange(96)
                    .updateInterval(1)
                    .build("skeletron_prime"));

    // Hardmode Endgame Bosses
    public static final DeferredHolder<EntityType<?>, EntityType<com.terraforge.rpg.boss.endgame.PlanteraEntity>> PLANTERA =
            ENTITIES.register("plantera", () -> EntityType.Builder.<com.terraforge.rpg.boss.endgame.PlanteraEntity>of(
                    com.terraforge.rpg.boss.endgame.PlanteraEntity::new, MobCategory.MONSTER)
                    .sized(3.5f, 3.5f)
                    .clientTrackingRange(128)
                    .updateInterval(1)
                    .build("plantera"));

    public static final DeferredHolder<EntityType<?>, EntityType<com.terraforge.rpg.boss.endgame.GolemEntity>> GOLEM =
            ENTITIES.register("golem", () -> EntityType.Builder.<com.terraforge.rpg.boss.endgame.GolemEntity>of(
                    com.terraforge.rpg.boss.endgame.GolemEntity::new, MobCategory.MONSTER)
                    .sized(3.0f, 4.0f)
                    .clientTrackingRange(96)
                    .updateInterval(1)
                    .build("golem"));

    public static final DeferredHolder<EntityType<?>, EntityType<com.terraforge.rpg.boss.endgame.DukeFishronEntity>> DUKE_FISHRON =
            ENTITIES.register("duke_fishron", () -> EntityType.Builder.<com.terraforge.rpg.boss.endgame.DukeFishronEntity>of(
                    com.terraforge.rpg.boss.endgame.DukeFishronEntity::new, MobCategory.MONSTER)
                    .sized(3.0f, 2.5f)
                    .clientTrackingRange(128)
                    .updateInterval(1)
                    .build("duke_fishron"));

    public static final DeferredHolder<EntityType<?>, EntityType<com.terraforge.rpg.boss.endgame.MoonLordEntity>> MOON_LORD =
            ENTITIES.register("moon_lord", () -> EntityType.Builder.<com.terraforge.rpg.boss.endgame.MoonLordEntity>of(
                    com.terraforge.rpg.boss.endgame.MoonLordEntity::new, MobCategory.MONSTER)
                    .sized(5.0f, 8.0f)
                    .clientTrackingRange(160)
                    .updateInterval(1)
                    .build("moon_lord"));

    public static void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(GREEN_SLIME.get(), TerraSlimeEntity.createAttributes(TerraSlimeEntity.SlimeVariant.GREEN).build());
        event.put(BLUE_SLIME.get(), TerraSlimeEntity.createAttributes(TerraSlimeEntity.SlimeVariant.BLUE).build());
        event.put(DEMON_EYE.get(), DemonEyeEntity.createAttributes().build());
        event.put(TERRA_ZOMBIE.get(), TerraZombieEntity.createAttributes().build());
        event.put(FACE_MONSTER.get(), com.terraforge.rpg.entity.mob.FaceMonsterEntity.createAttributes().build());
        event.put(EYE_OF_CTHULHU.get(), EyeOfCthulhuEntity.createAttributes().build());
        event.put(SERVANT_OF_CTHULHU.get(), ServantOfCthulhuEntity.createAttributes().build());
        event.put(WALL_OF_FLESH.get(), com.terraforge.rpg.boss.prehardmode.WallOfFleshEntity.createAttributes().build());
        event.put(THE_HUNGRY.get(), com.terraforge.rpg.boss.prehardmode.TheHungryEntity.createAttributes().build());
        event.put(KING_SLIME.get(), com.terraforge.rpg.boss.prehardmode.KingSlimeEntity.createAttributes().build());
        event.put(GOBLIN_PEON.get(), com.terraforge.rpg.entity.mob.goblin.GoblinPeonEntity.createAttributes().build());
        event.put(GOBLIN_THIEF.get(), com.terraforge.rpg.entity.mob.goblin.GoblinThiefEntity.createAttributes().build());
        event.put(GOBLIN_WARRIOR.get(), com.terraforge.rpg.entity.mob.goblin.GoblinWarriorEntity.createAttributes().build());
        event.put(GOBLIN_SORCERER.get(), com.terraforge.rpg.entity.mob.goblin.GoblinSorcererEntity.createAttributes().build());
        event.put(GUIDE.get(), com.terraforge.rpg.entity.npc.GuideEntity.createTownNpcAttributes().build());
        event.put(MERCHANT.get(), com.terraforge.rpg.entity.npc.MerchantEntity.createTownNpcAttributes().build());
        event.put(NURSE.get(), com.terraforge.rpg.entity.npc.NurseEntity.createTownNpcAttributes().build());
        event.put(GOBLIN_TINKERER.get(), com.terraforge.rpg.entity.npc.GoblinTinkererEntity.createTownNpcAttributes().build());
        event.put(SLIME_MOUNT.get(), com.terraforge.rpg.entity.mount.SlimeMountEntity.createMountAttributes().build());

        // Mechanical Boss Attributes
        event.put(RETINAZER.get(), com.terraforge.rpg.boss.hardmode.RetinazerEntity.createAttributes().build());
        event.put(SPAZMATISM.get(), com.terraforge.rpg.boss.hardmode.SpazmatismEntity.createAttributes().build());
        event.put(DESTROYER_PROBE.get(), com.terraforge.rpg.boss.hardmode.DestroyerProbeEntity.createAttributes().build());
        event.put(THE_DESTROYER.get(), com.terraforge.rpg.boss.hardmode.TheDestroyerEntity.createAttributes().build());
        event.put(SKELETRON_PRIME.get(), com.terraforge.rpg.boss.hardmode.SkeletronPrimeEntity.createAttributes().build());

        // Endgame Boss Attributes
        event.put(PLANTERA.get(), com.terraforge.rpg.boss.endgame.PlanteraEntity.createAttributes().build());
        event.put(GOLEM.get(), com.terraforge.rpg.boss.endgame.GolemEntity.createAttributes().build());
        event.put(DUKE_FISHRON.get(), com.terraforge.rpg.boss.endgame.DukeFishronEntity.createAttributes().build());
        event.put(MOON_LORD.get(), com.terraforge.rpg.boss.endgame.MoonLordEntity.createAttributes().build());
    }

    private ModEntities() {}
}
