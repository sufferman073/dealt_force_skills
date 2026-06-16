package com.rzy.dealt_force_skills.command;

import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.rzy.dealt_force_skills.DealtForceSkillsMod;
import com.rzy.dealt_force_skills.shop.DealtCurrencyProvider;
import com.rzy.dealt_force_skills.shop.DealtCurrencyRegistry;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Collection;
import java.util.Optional;

@Mod.EventBusSubscriber(modid = DealtForceSkillsMod.MODID)
public final class ShowMeTheDealtCoinCommand {
    private static final SimpleCommandExceptionType NO_ACTIVE_CURRENCY =
            new SimpleCommandExceptionType(Component.translatable(
                    "commands.dealt_force_skills.showmethedealtcoin.no_active_currency"));

    private ShowMeTheDealtCoinCommand() {
    }

    @SubscribeEvent
    public static void register(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("showmethedealtcoin")
                .requires(source -> source.hasPermission(2))
                .then(Commands.argument("targets", EntityArgument.players())
                        .then(Commands.argument("amount", LongArgumentType.longArg(1L))
                                .executes(context -> execute(
                                        context.getSource(),
                                        EntityArgument.getPlayers(context, "targets"),
                                        LongArgumentType.getLong(context, "amount")
                                )))));
    }

    private static int execute(CommandSourceStack source, Collection<ServerPlayer> targets, long amount)
            throws CommandSyntaxException {
        int granted = 0;
        int skipped = 0;
        ServerPlayer singleTarget = null;
        DealtCurrencyProvider singleProvider = null;
        long singleBalance = 0L;

        for (ServerPlayer target : targets) {
            Optional<DealtCurrencyProvider> provider = DealtCurrencyRegistry.find(target);
            if (provider.isEmpty()) {
                skipped++;
                continue;
            }
            DealtCurrencyProvider active = provider.get();
            long balance = active.grant(target, amount);
            target.sendSystemMessage(Component.translatable(
                    "commands.dealt_force_skills.showmethedealtcoin.received",
                    amount, active.displayName(), balance));
            granted++;
            singleTarget = target;
            singleProvider = active;
            singleBalance = balance;
        }

        if (granted == 0) {
            throw NO_ACTIVE_CURRENCY.create();
        }
        if (targets.size() == 1 && singleTarget != null && singleProvider != null) {
            ServerPlayer target = singleTarget;
            DealtCurrencyProvider provider = singleProvider;
            long balance = singleBalance;
            source.sendSuccess(() -> Component.translatable(
                    "commands.dealt_force_skills.showmethedealtcoin.success",
                    amount, provider.displayName(), target.getDisplayName(), balance), true);
        } else {
            int grantedCount = granted;
            int skippedCount = skipped;
            source.sendSuccess(() -> Component.translatable(
                    "commands.dealt_force_skills.showmethedealtcoin.success.multiple",
                    amount, grantedCount, skippedCount), true);
        }
        return granted;
    }
}
