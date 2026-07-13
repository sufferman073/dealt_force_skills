package com.rzy.dealt_force_skills.network;

import com.rzy.dealt_force_skills.block.HvkAdvancedStandardTemplateConstructorBlockEntity.RecipeSearchResult;
import com.rzy.dealt_force_skills.block.HvkAdvancedStandardTemplateConstructorBlockEntity.RecipeView;
import com.rzy.dealt_force_skills.client.screen.HvkConstructorScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.List;
import java.util.function.Supplier;

public class S2C_HvkConstructorRecipes {
    private final BlockPos pos;
    private final String query;
    private final int page;
    private final int total;
    private final List<RecipeView> recipes;
    private final boolean disassembler;

    public S2C_HvkConstructorRecipes(BlockPos pos, String query, RecipeSearchResult result) {
        this(pos, query, result == null ? 0 : result.page(), result == null ? 0 : result.total(),
                result == null ? List.of() : result.recipes(), false);
    }

    public S2C_HvkConstructorRecipes(BlockPos pos, String query, RecipeSearchResult result, boolean disassembler) {
        this(pos, query, result == null ? 0 : result.page(), result == null ? 0 : result.total(),
                result == null ? List.of() : result.recipes(), disassembler);
    }

    private S2C_HvkConstructorRecipes(BlockPos pos, String query, int page, int total, List<RecipeView> recipes,
                                      boolean disassembler) {
        this.pos = pos;
        this.query = query == null ? "" : query;
        this.page = Math.max(0, page);
        this.total = Math.max(0, total);
        this.recipes = recipes == null ? List.of() : List.copyOf(recipes);
        this.disassembler = disassembler;
    }

    public static void encode(S2C_HvkConstructorRecipes msg, FriendlyByteBuf buf) {
        buf.writeBlockPos(msg.pos);
        buf.writeUtf(msg.query);
        buf.writeVarInt(msg.page);
        buf.writeVarInt(msg.total);
        buf.writeBoolean(msg.disassembler);
        S2C_OpenHvkConstructor.writeRecipes(msg.recipes, buf);
    }

    public static S2C_HvkConstructorRecipes decode(FriendlyByteBuf buf) {
        BlockPos pos = buf.readBlockPos();
        String query = buf.readUtf(128);
        int page = buf.readVarInt();
        int total = buf.readVarInt();
        boolean disassembler = buf.readBoolean();
        return new S2C_HvkConstructorRecipes(pos, query, page, total,
                S2C_OpenHvkConstructor.readRecipes(buf), disassembler);
    }

    public static void handle(S2C_HvkConstructorRecipes msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(
                Dist.CLIENT,
                () -> () -> HvkConstructorScreen.updateRecipes(msg.pos, msg.query, msg.page, msg.total, msg.recipes,
                        msg.disassembler)
        ));
        ctx.get().setPacketHandled(true);
    }
}
