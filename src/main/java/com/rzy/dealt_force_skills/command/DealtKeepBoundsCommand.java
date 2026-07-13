package com.rzy.dealt_force_skills.command;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.rzy.dealt_force_skills.DealtForceSkillsMod;
import com.rzy.dealt_force_skills.shop.DealtKeepBoundsManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Collection;

/**
 * {@code /dealtkeepbounds <all|players> <true|false>}
 *
 * <ul>
 *   <li>{@code all true/false} — server-wide default for players without a personal override.
 *       Applies to currently online and future players (via the default).</li>
 *   <li>player selector — per-player override that takes priority over {@code all}.</li>
 *   <li>{@code true} (default) — keep purchase progress and special currency across matchstart
 *       and rejoin.</li>
 *   <li>{@code false} — clear purchase progress and special currency on the next
 *       {@code /dealtmatch start} (initial start only, not mid-match round reset) and on rejoin.</li>
 * </ul>
 */
@Mod.EventBusSubscriber(modid = DealtForceSkillsMod.MODID)
public final class DealtKeepBoundsCommand {
    private DealtKeepBoundsCommand() {
    }

    @SubscribeEvent
    public static void register(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("dealtkeepbounds")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("all")
                        .then(Commands.argument("keep", BoolArgumentType.bool())
                                .executes(context -> setAll(
                                        context.getSource(),
                                        BoolArgumentType.getBool(context, "keep")))))
                .then(Commands.argument("targets", EntityArgument.players())
                        .then(Commands.argument("keep", BoolArgumentType.bool())
                                .executes(context -> setPlayers(
                                        context.getSource(),
                                        EntityArgument.getPlayers(context, "targets"),
                                        BoolArgumentType.getBool(context, "keep"))))));
    }

    private static int setAll(CommandSourceStack source, boolean keep) {
        DealtKeepBoundsManager.setServerDefault(source.getServer(), keep);
        source.sendSuccess(() -> Component.translatable(
                keep
                        ? "commands.dealt_force_skills.dealtkeepbounds.all.true"
                        : "commands.dealt_force_skills.dealtkeepbounds.all.false"), true);
        return 1;
    }

    private static int setPlayers(CommandSourceStack source, Collection<ServerPlayer> targets, boolean keep) {
        int changed = 0;
        for (ServerPlayer player : targets) {
            DealtKeepBoundsManager.setPlayerOverride(player, keep);
            changed++;
        }
        int count = changed;
        if (targets.size() == 1) {
            ServerPlayer only = targets.iterator().next();
            source.sendSuccess(() -> Component.translatable(
                    keep
                            ? "commands.dealt_force_skills.dealtkeepbounds.player.true"
                            : "commands.dealt_force_skills.dealtkeepbounds.player.false",
                    only.getDisplayName()), true);
        } else {
            source.sendSuccess(() -> Component.translatable(
                    keep
                            ? "commands.dealt_force_skills.dealtkeepbounds.players.true"
                            : "commands.dealt_force_skills.dealtkeepbounds.players.false",
                    count), true);
        }
        return changed;
    }
}
