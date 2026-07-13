package com.rzy.dealt_force_skills.network;

import com.rzy.dealt_force_skills.block.HvkAdvancedStandardTemplateConstructorBlockEntity.RecipeView;
import com.rzy.dealt_force_skills.block.HvkAdvancedStandardTemplateConstructorBlockEntity.RecipeSearchResult;
import com.rzy.dealt_force_skills.client.screen.HvkConstructorScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class S2C_OpenHvkConstructor {
    private static final int MAX_RECIPES = 256;
    private static final int MAX_INGREDIENTS = 256;

    private final BlockPos pos;
    private final String query;
    private final int page;
    private final int total;
    private final List<RecipeView> recipes;
    private final boolean disassembler;

    public S2C_OpenHvkConstructor(BlockPos pos, String query, RecipeSearchResult result) {
        this(pos, query, result == null ? 0 : result.page(), result == null ? 0 : result.total(),
                result == null ? List.of() : result.recipes(), false);
    }

    public S2C_OpenHvkConstructor(BlockPos pos, String query, RecipeSearchResult result, boolean disassembler) {
        this(pos, query, result == null ? 0 : result.page(), result == null ? 0 : result.total(),
                result == null ? List.of() : result.recipes(), disassembler);
    }

    private S2C_OpenHvkConstructor(BlockPos pos, String query, int page, int total, List<RecipeView> recipes,
                                   boolean disassembler) {
        this.pos = pos;
        this.query = query == null ? "" : query;
        this.page = Math.max(0, page);
        this.total = Math.max(0, total);
        this.recipes = recipes == null ? List.of() : List.copyOf(recipes);
        this.disassembler = disassembler;
    }

    public static void encode(S2C_OpenHvkConstructor msg, FriendlyByteBuf buf) {
        buf.writeBlockPos(msg.pos);
        buf.writeUtf(msg.query);
        buf.writeVarInt(msg.page);
        buf.writeVarInt(msg.total);
        buf.writeBoolean(msg.disassembler);
        writeRecipes(msg.recipes, buf);
    }

    public static S2C_OpenHvkConstructor decode(FriendlyByteBuf buf) {
        BlockPos pos = buf.readBlockPos();
        String query = buf.readUtf(128);
        int page = buf.readVarInt();
        int total = buf.readVarInt();
        boolean disassembler = buf.readBoolean();
        return new S2C_OpenHvkConstructor(pos, query, page, total, readRecipes(buf), disassembler);
    }

    public static void handle(S2C_OpenHvkConstructor msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(
                Dist.CLIENT,
                () -> () -> HvkConstructorScreen.open(msg.pos, msg.query, msg.page, msg.total, msg.recipes,
                        msg.disassembler)
        ));
        ctx.get().setPacketHandled(true);
    }

    static void writeRecipes(List<RecipeView> recipes, FriendlyByteBuf buf) {
        buf.writeVarInt(Math.min(MAX_RECIPES, recipes.size()));
        for (int i = 0; i < Math.min(MAX_RECIPES, recipes.size()); i++) {
            RecipeView recipe = recipes.get(i);
            buf.writeUtf(recipe.id().toString());
            buf.writeItem(recipe.result());
            List<ItemStack> ingredients = recipe.ingredients();
            buf.writeVarInt(Math.min(MAX_INGREDIENTS, ingredients.size()));
            for (int j = 0; j < Math.min(MAX_INGREDIENTS, ingredients.size()); j++) {
                buf.writeItem(ingredients.get(j));
            }
        }
    }

    static List<RecipeView> readRecipes(FriendlyByteBuf buf) {
        int size = Math.max(0, Math.min(MAX_RECIPES, buf.readVarInt()));
        List<RecipeView> recipes = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            ResourceLocation id = readResourceLocation(buf);
            ItemStack result = buf.readItem();
            int ingredientCount = Math.max(0, Math.min(MAX_INGREDIENTS, buf.readVarInt()));
            List<ItemStack> ingredients = new ArrayList<>(ingredientCount);
            for (int j = 0; j < ingredientCount; j++) {
                ingredients.add(buf.readItem());
            }
            recipes.add(new RecipeView(id, result, ingredients));
        }
        return recipes;
    }

    private static ResourceLocation readResourceLocation(FriendlyByteBuf buf) {
        String value = buf.readUtf(512);
        try {
            return ResourceLocation.parse(value);
        } catch (RuntimeException ex) {
            return ResourceLocation.fromNamespaceAndPath("minecraft", "air");
        }
    }
}
