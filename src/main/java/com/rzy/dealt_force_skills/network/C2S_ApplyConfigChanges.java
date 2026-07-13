package com.rzy.dealt_force_skills.network;

import com.rzy.dealt_force_skills.config.ConfigAccess;
import com.rzy.dealt_force_skills.config.ConfigEntryData;
import com.rzy.dealt_force_skills.config.ConfigFileId;
import com.rzy.dealt_force_skills.config.ConfigHotReload;
import com.rzy.dealt_force_skills.config.ConfigValueKind;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

public final class C2S_ApplyConfigChanges {
    private static final int MAX_CHANGES = 512;

    private final List<Change> changes;

    public C2S_ApplyConfigChanges(List<Change> changes) {
        this.changes = changes == null ? List.of() : List.copyOf(changes);
    }

    public static void encode(C2S_ApplyConfigChanges msg, FriendlyByteBuf buf) {
        buf.writeVarInt(msg.changes.size());
        for (Change change : msg.changes) {
            buf.writeEnum(change.file());
            buf.writeUtf(change.path(), 512);
            buf.writeEnum(change.kind());
            buf.writeUtf(change.valueText(), 1024);
        }
    }

    public static C2S_ApplyConfigChanges decode(FriendlyByteBuf buf) {
        int size = Math.min(MAX_CHANGES, buf.readVarInt());
        List<Change> changes = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            changes.add(new Change(
                    buf.readEnum(ConfigFileId.class),
                    buf.readUtf(512),
                    buf.readEnum(ConfigValueKind.class),
                    buf.readUtf(1024)));
        }
        return new C2S_ApplyConfigChanges(changes);
    }

    public static void handle(C2S_ApplyConfigChanges msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) {
                return;
            }
            if (!player.hasPermissions(2)) {
                player.sendSystemMessage(Component.translatable("config.dealt_force_skills.error.no_permission"));
                NetworkHandler.sendToPlayer(new S2C_ConfigApplyResult(false, 0, "no_permission"), player);
                return;
            }
            if (msg.changes.isEmpty()) {
                NetworkHandler.sendToPlayer(new S2C_ConfigApplyResult(true, 0, "empty"), player);
                return;
            }
            int applied = 0;
            Set<ConfigFileId> touched = new HashSet<>();
            for (Change change : msg.changes) {
                if (change.file() == null || change.file().clientLocal()) {
                    continue;
                }
                if (ConfigAccess.applyChange(change.file(), change.path(), change.kind(), change.valueText())) {
                    applied++;
                    touched.add(change.file());
                }
            }
            for (ConfigFileId file : touched) {
                ConfigAccess.flush(file);
            }
            ConfigHotReload.Result reload = ConfigHotReload.reloadAll();
            boolean ok = applied > 0 && reload.success();
            NetworkHandler.sendToPlayer(new S2C_ConfigApplyResult(ok, applied, ok ? "ok" : "partial"), player);
            if (ok) {
                player.sendSystemMessage(Component.translatable(
                        "config.dealt_force_skills.apply.success", applied));
            } else {
                player.sendSystemMessage(Component.translatable(
                        "config.dealt_force_skills.apply.partial", applied));
            }
        });
        ctx.get().setPacketHandled(true);
    }

    public record Change(ConfigFileId file, String path, ConfigValueKind kind, String valueText) {
        public static Change from(ConfigEntryData entry, String newValue) {
            return new Change(entry.file(), entry.path(), entry.kind(), newValue);
        }
    }
}
