package com.terraforge.rpg.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.terraforge.rpg.boss.prehardmode.EyeOfCthulhuEntity;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import java.util.Comparator;
import java.util.List;

/**
 * Developer and debugging commands for TerraForge RPG.
 * Allows forcing animation states, rigging verification, and performance profiling.
 */
public final class TerraForgeCommands {

    private TerraForgeCommands() {}

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();

        dispatcher.register(
            Commands.literal("terraforge")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("debug")
                    .then(Commands.literal("eye")
                        .then(Commands.literal("state")
                            .then(Commands.argument("state", StringArgumentType.word())
                                .suggests((ctx, builder) -> {
                                    for (EyeOfCthulhuEntity.EyeAnimState s : EyeOfCthulhuEntity.EyeAnimState.values()) {
                                        builder.suggest(s.name().toLowerCase());
                                    }
                                    return builder.buildFuture();
                                })
                                .executes(TerraForgeCommands::setEyeState)
                            )
                        )
                    )
                )
        );
    }

    private static int setEyeState(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack source = ctx.getSource();
        String stateName = StringArgumentType.getString(ctx, "state");

        EyeOfCthulhuEntity.EyeAnimState targetState = null;
        for (EyeOfCthulhuEntity.EyeAnimState s : EyeOfCthulhuEntity.EyeAnimState.values()) {
            if (s.name().equalsIgnoreCase(stateName)) {
                targetState = s;
                break;
            }
        }

        if (targetState == null) {
            source.sendFailure(Component.literal("Unknown Eye animation state: " + stateName));
            return 0;
        }

        ServerLevel level = source.getLevel();
        Vec3 pos = source.getPosition();
        AABB searchBox = new AABB(pos.x - 64.0, pos.y - 64.0, pos.z - 64.0, pos.x + 64.0, pos.y + 64.0, pos.z + 64.0);

        List<EyeOfCthulhuEntity> eyes = level.getEntitiesOfClass(EyeOfCthulhuEntity.class, searchBox);
        if (eyes.isEmpty()) {
            source.sendFailure(Component.literal("No Eye of Cthulhu found within 64 blocks."));
            return 0;
        }

        // Target nearest eye
        eyes.sort(Comparator.comparingDouble(e -> e.distanceToSqr(pos)));
        EyeOfCthulhuEntity targetEye = eyes.get(0);

        targetEye.setAnimState(targetState);
        final EyeOfCthulhuEntity.EyeAnimState finalState = targetState;
        source.sendSuccess(() -> Component.literal(String.format("Set Eye of Cthulhu [%s] anim state to %s",
                targetEye.getStringUUID().substring(0, 8), finalState.name())), true);

        return 1;
    }
}
