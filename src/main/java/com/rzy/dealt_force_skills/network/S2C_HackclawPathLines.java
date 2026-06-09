package com.rzy.dealt_force_skills.network;

import com.rzy.dealt_force_skills.client.visual.HackclawPathLineRenderer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class S2C_HackclawPathLines {
    private final List<Line> lines;

    public S2C_HackclawPathLines(List<Line> lines) {
        this.lines = List.copyOf(lines);
    }

    public static void encode(S2C_HackclawPathLines msg, FriendlyByteBuf buf) {
        buf.writeVarInt(msg.lines.size());
        for (Line line : msg.lines) {
            buf.writeVarInt(line.sourceEntityId());
            buf.writeVarInt(line.targetEntityId());
            buf.writeDouble(line.from().x);
            buf.writeDouble(line.from().y);
            buf.writeDouble(line.from().z);
            buf.writeDouble(line.to().x);
            buf.writeDouble(line.to().y);
            buf.writeDouble(line.to().z);
            buf.writeBoolean(line.primary());
            buf.writeVarInt(line.ticks());
        }
    }

    public static S2C_HackclawPathLines decode(FriendlyByteBuf buf) {
        int count = buf.readVarInt();
        List<Line> lines = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            int sourceEntityId = buf.readVarInt();
            int targetEntityId = buf.readVarInt();
            Vec3 from = new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble());
            Vec3 to = new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble());
            boolean primary = buf.readBoolean();
            int ticks = buf.readVarInt();
            lines.add(new Line(sourceEntityId, targetEntityId, from, to, primary, ticks));
        }
        return new S2C_HackclawPathLines(lines);
    }

    public static void handle(S2C_HackclawPathLines msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(
                Dist.CLIENT,
                () -> () -> HackclawPathLineRenderer.setLines(msg.lines)
        ));
        ctx.get().setPacketHandled(true);
    }

    public record Line(int sourceEntityId, int targetEntityId, Vec3 from, Vec3 to, boolean primary, int ticks) {
    }
}
