package com.rzy.dealt_force_skills.block;

import com.rzy.dealt_force_skills.block.AdvancedWorkBlockData.EntryView;
import com.rzy.dealt_force_skills.compat.JustEnoughCharactersCompat;
import com.rzy.dealt_force_skills.item.HvkTreasureChestBoxItem;
import com.rzy.dealt_force_skills.network.NetworkHandler;
import com.rzy.dealt_force_skills.network.S2C_OpenAdvancedWorkBlock;
import com.rzy.dealt_force_skills.network.S2C_UpdateAdvancedWorkBlock;
import com.rzy.dealt_force_skills.registry.ModBlockEntities;
import com.rzy.dealt_force_skills.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootDataType;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.Tags;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class HvkAdvancedTreasureCompassBlockEntity extends BlockEntity {
    private static final int LOOT_PREVIEW_ROLLS = 48;
    private static final int LOOT_BOX_ROLLS = 192;
    private static final int MAX_ACTION_COUNT = 64;
    private UUID ownerId;

    public HvkAdvancedTreasureCompassBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.HVK_ADVANCED_TREASURE_COMPASS.get(), pos, state);
    }

    public InteractionResult use(net.minecraft.world.entity.player.Player player) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.CONSUME;
        }
        if (SpecialWorkBlockConfig.denyUse(level, player,
                SpecialWorkBlockConfig.isHvkAdvancedTreasureCompassEnabled())) {
            return InteractionResult.CONSUME;
        }
        ownerId = serverPlayer.getUUID();
        open(serverPlayer, AdvancedWorkBlockData.MODE_TREASURE_CHEST, "");
        return InteractionResult.CONSUME;
    }

    public void open(ServerPlayer player, int mode, String query) {
        if (!canUse(player)) {
            return;
        }
        ownerId = player.getUUID();
        NetworkHandler.sendToPlayer(new S2C_OpenAdvancedWorkBlock(data(player, mode, query, 0)), player);
    }

    public void sendData(ServerPlayer player, int mode, String query, int page) {
        if (!canUse(player)) {
            return;
        }
        ownerId = player.getUUID();
        NetworkHandler.sendToPlayer(new S2C_UpdateAdvancedWorkBlock(data(player, mode, query, page)), player);
    }

    public void performAction(ServerPlayer player, int mode, ResourceLocation id, int action, int requestedCount,
                              String query, int page) {
        if (id == null || !canUse(player)) {
            return;
        }
        ownerId = player.getUUID();
        if (mode == AdvancedWorkBlockData.MODE_TREASURE_ORE) {
            craftOre(player, id, clamp(requestedCount, 1, MAX_ACTION_COUNT));
        } else {
            createChestBox(player, id);
        }
        sendData(player, mode, query, page);
    }

    private void createChestBox(ServerPlayer player, ResourceLocation lootId) {
        if (!(level instanceof ServerLevel serverLevel) || !isChestLoot(lootId)) {
            player.displayClientMessage(Component.translatable("message.dealt_force_skills.hvk_treasure.loot_missing"), true);
            return;
        }
        List<ItemStack> loot = chestLoot(player, serverLevel, Vec3.atCenterOf(worldPosition), lootId, LOOT_PREVIEW_ROLLS);
        if (loot.isEmpty()) {
            player.displayClientMessage(Component.translatable("message.dealt_force_skills.hvk_treasure.no_loot"), true);
            return;
        }
        List<WorkBlockStorageAccess.SourceSlot> sources = WorkBlockStorageAccess.collectSources(level, worldPosition, player);
        ItemStack payment = bestLootPayment(loot, sources);
        if (payment.isEmpty()) {
            player.displayClientMessage(Component.translatable("message.dealt_force_skills.hvk_treasure.payment_missing"), true);
            return;
        }
        int required = payment.getMaxStackSize();
        int removed = WorkBlockStorageAccess.consumeItem(sources, payment.getItem(), required);
        if (removed < required) {
            player.displayClientMessage(Component.translatable("message.dealt_force_skills.hvk_treasure.payment_missing"), true);
            return;
        }
        WorkBlockStorageAccess.giveOrDrop(player, HvkTreasureChestBoxItem.create(lootId, lootName(lootId)));
        player.displayClientMessage(Component.translatable("message.dealt_force_skills.hvk_treasure.box_created",
                lootName(lootId)), true);
        playUseSound();
    }

    private void craftOre(ServerPlayer player, ResourceLocation oreId, int count) {
        Item item = ForgeRegistries.ITEMS.getValue(oreId);
        if (item == null || item == Items.AIR || !isOreItem(item)) {
            player.displayClientMessage(Component.translatable("message.dealt_force_skills.hvk_treasure.ore_missing"), true);
            return;
        }
        List<WorkBlockStorageAccess.SourceSlot> sources = WorkBlockStorageAccess.collectSources(level, worldPosition, player);
        StonePayment payment = bestStonePayment(sources);
        if (payment == null) {
            player.displayClientMessage(Component.translatable("message.dealt_force_skills.hvk_treasure.stone_missing"), true);
            return;
        }
        int removed = WorkBlockStorageAccess.consumeItem(sources, payment.item(), payment.count());
        if (removed < payment.count()) {
            player.displayClientMessage(Component.translatable("message.dealt_force_skills.hvk_treasure.stone_missing"), true);
            return;
        }
        WorkBlockStorageAccess.giveWithBlockLootBonus(player, level, worldPosition, new ItemStack(item, count));
        player.displayClientMessage(Component.translatable("message.dealt_force_skills.hvk_treasure.ore_created",
                count, new ItemStack(item).getHoverName()), true);
        playUseSound();
    }

    private AdvancedWorkBlockData data(ServerPlayer player, int requestedMode, String query, int requestedPage) {
        int mode = requestedMode == AdvancedWorkBlockData.MODE_TREASURE_ORE
                ? AdvancedWorkBlockData.MODE_TREASURE_ORE
                : AdvancedWorkBlockData.MODE_TREASURE_CHEST;
        String normalized = normalizeQuery(query);
        List<EntryView> entries = mode == AdvancedWorkBlockData.MODE_TREASURE_ORE
                ? oreEntries(player, normalized)
                : chestEntries(player, normalized);
        int maxPage = entries.isEmpty() ? 0 : (entries.size() - 1) / AdvancedWorkBlockData.PAGE_SIZE;
        int page = clamp(requestedPage, 0, maxPage);
        int from = page * AdvancedWorkBlockData.PAGE_SIZE;
        int to = Math.min(entries.size(), from + AdvancedWorkBlockData.PAGE_SIZE);
        return new AdvancedWorkBlockData(worldPosition, AdvancedWorkBlockData.BLOCK_HVK_TREASURE_COMPASS, mode,
                normalized, page, entries.size(), 0.0D, entries.subList(from, to));
    }

    private List<EntryView> chestEntries(ServerPlayer player, String query) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return List.of();
        }
        List<WorkBlockStorageAccess.SourceSlot> sources = WorkBlockStorageAccess.collectSources(level, worldPosition, player);
        Map<Item, Integer> stock = WorkBlockStorageAccess.countItems(sources);
        List<EntryView> entries = new ArrayList<>();
        for (ResourceLocation id : chestLootIds(serverLevel)) {
            if (!matches(id, lootName(id), query)) {
                continue;
            }
            List<ItemStack> outputs = chestLoot(player, serverLevel, Vec3.atCenterOf(worldPosition), id, LOOT_PREVIEW_ROLLS);
            if (outputs.isEmpty()) {
                continue;
            }
            int available = maxAvailablePaymentStacks(outputs, stock);
            ItemStack icon = HvkTreasureChestBoxItem.create(id, lootName(id));
            String detail = Component.translatable("screen.dealt_force_skills.hvk_treasure.payment_stacks", available)
                    .getString();
            entries.add(new EntryView(id, icon, lootName(id), detail, outputs, available, 0.0D));
        }
        entries.sort(Comparator.comparing(EntryView::name).thenComparing(entry -> entry.id().toString()));
        return entries;
    }

    private List<EntryView> oreEntries(ServerPlayer player, String query) {
        List<WorkBlockStorageAccess.SourceSlot> sources = WorkBlockStorageAccess.collectSources(level, worldPosition, player);
        int stoneStacks = availableStoneStacks(sources);
        List<EntryView> entries = new ArrayList<>();
        for (Block block : ForgeRegistries.BLOCKS.getValues()) {
            if (!isOreBlock(block)) {
                continue;
            }
            Item item = block.asItem();
            ResourceLocation id = ForgeRegistries.ITEMS.getKey(item);
            if (id == null || !matches(id, new ItemStack(item).getHoverName().getString(), query)) {
                continue;
            }
            ItemStack stack = new ItemStack(item);
            String detail = Component.translatable("screen.dealt_force_skills.hvk_treasure.stone_stacks", stoneStacks)
                    .getString();
            entries.add(new EntryView(id, stack, stack.getHoverName().getString(), detail,
                    List.of(stack.copy()), stoneStacks, 64.0D));
        }
        entries.sort(Comparator.comparing(EntryView::name).thenComparing(entry -> entry.id().toString()));
        return entries;
    }

    public static List<ItemStack> chestLoot(ServerPlayer player, ServerLevel level, Vec3 origin,
                                            ResourceLocation lootId, int rolls) {
        if (player == null || level == null || lootId == null) {
            return List.of();
        }
        LootTable table = level.getServer().getLootData().getLootTable(lootId);
        LootParams params;
        try {
            params = new LootParams.Builder(level)
                    .withParameter(LootContextParams.ORIGIN, origin)
                    .withLuck(player.getLuck())
                    .create(LootContextParamSets.CHEST);
        } catch (RuntimeException ignored) {
            return List.of();
        }
        Map<Item, ItemStack> prototypes = new LinkedHashMap<>();
        Map<Item, Integer> maxCounts = new HashMap<>();
        int safeRolls = Math.max(1, Math.min(rolls, 256));
        for (int i = 0; i < safeRolls; i++) {
            Map<Item, Integer> rollCounts = new HashMap<>();
            for (ItemStack stack : table.getRandomItems(params)) {
                if (stack.isEmpty() || stack.is(Items.AIR)) {
                    continue;
                }
                prototypes.putIfAbsent(stack.getItem(), stack.copy());
                rollCounts.merge(stack.getItem(), stack.getCount(), Integer::sum);
            }
            for (Map.Entry<Item, Integer> entry : rollCounts.entrySet()) {
                maxCounts.merge(entry.getKey(), entry.getValue(), Math::max);
            }
        }
        List<ItemStack> result = new ArrayList<>();
        for (Map.Entry<Item, ItemStack> entry : prototypes.entrySet()) {
            ItemStack stack = entry.getValue().copy();
            stack.setCount(Math.max(1, maxCounts.getOrDefault(entry.getKey(), 1)));
            result.add(stack);
        }
        result.sort(Comparator.comparing(stack -> itemKey(stack.getItem())));
        return List.copyOf(result);
    }

    public static List<ItemStack> chestBoxLoot(ServerPlayer player, ServerLevel level, ResourceLocation lootId) {
        return chestLoot(player, level, player.position(), lootId, LOOT_BOX_ROLLS);
    }

    private static Collection<ResourceLocation> chestLootIds(ServerLevel level) {
        return level.getServer().getLootData().getKeys(LootDataType.TABLE).stream()
                .filter(HvkAdvancedTreasureCompassBlockEntity::isChestLoot)
                .sorted(Comparator.comparing(ResourceLocation::toString))
                .toList();
    }

    private static boolean isChestLoot(ResourceLocation id) {
        return id != null && id.getPath().startsWith("chests/");
    }

    private static ItemStack bestLootPayment(List<ItemStack> loot, List<WorkBlockStorageAccess.SourceSlot> sources) {
        Map<Item, Integer> stock = WorkBlockStorageAccess.countItems(sources);
        ItemStack best = ItemStack.EMPTY;
        int bestStacks = 0;
        int bestCount = 0;
        for (ItemStack stack : loot) {
            int required = stack.getMaxStackSize();
            int available = stock.getOrDefault(stack.getItem(), 0);
            int stacks = required <= 0 ? 0 : available / required;
            if (stacks > bestStacks || stacks == bestStacks && available > bestCount) {
                ItemStack candidate = stack.copy();
                candidate.setCount(Math.max(1, required));
                best = candidate;
                bestStacks = stacks;
                bestCount = available;
            }
        }
        return !best.isEmpty() && bestStacks > 0 ? best : ItemStack.EMPTY;
    }

    private static int maxAvailablePaymentStacks(List<ItemStack> loot, Map<Item, Integer> stock) {
        int max = 0;
        for (ItemStack stack : loot) {
            int required = stack.getMaxStackSize();
            if (required > 0) {
                max = Math.max(max, stock.getOrDefault(stack.getItem(), 0) / required);
            }
        }
        return max;
    }

    private static int availableStoneStacks(List<WorkBlockStorageAccess.SourceSlot> sources) {
        int stacks = 0;
        for (WorkBlockStorageAccess.SourceSlot source : sources) {
            ItemStack stack = source.stack();
            if (!stack.isEmpty() && isStonePayment(stack)) {
                stacks += stack.getCount() / stack.getMaxStackSize();
            }
        }
        return stacks;
    }

    private static StonePayment bestStonePayment(List<WorkBlockStorageAccess.SourceSlot> sources) {
        for (WorkBlockStorageAccess.SourceSlot source : sources) {
            ItemStack stack = source.stack();
            if (!stack.isEmpty() && isStonePayment(stack) && stack.getCount() >= stack.getMaxStackSize()) {
                return new StonePayment(stack.getItem(), stack.getMaxStackSize());
            }
        }
        return null;
    }

    private static boolean isOreBlock(Block block) {
        if (block == null || block.asItem() == Items.AIR) {
            return false;
        }
        ItemStack item = new ItemStack(block.asItem());
        ResourceLocation id = ForgeRegistries.BLOCKS.getKey(block);
        String path = id == null ? "" : id.getPath();
        return block.defaultBlockState().is(Tags.Blocks.ORES)
                || item.is(Tags.Items.ORES)
                || path.endsWith("_ore")
                || path.contains("_ore_")
                || path.equals("ancient_debris");
    }

    private static boolean isOreItem(Item item) {
        if (item == null || item == Items.AIR) {
            return false;
        }
        if (new ItemStack(item).is(Tags.Items.ORES)) {
            return true;
        }
        if (item instanceof BlockItem blockItem) {
            return isOreBlock(blockItem.getBlock());
        }
        return false;
    }

    private static boolean isStonePayment(ItemStack stack) {
        if (stack.isEmpty() || stack.is(Tags.Items.ORES)) {
            return false;
        }
        if (stack.is(Tags.Items.STONE) || stack.is(Tags.Items.COBBLESTONE)) {
            return true;
        }
        ResourceLocation id = ForgeRegistries.ITEMS.getKey(stack.getItem());
        String path = id == null ? "" : id.getPath();
        if (path.contains("redstone")) {
            return false;
        }
        if (stack.getItem() instanceof BlockItem blockItem) {
            BlockState state = blockItem.getBlock().defaultBlockState();
            if (state.is(Tags.Blocks.STONE) || state.is(Tags.Blocks.COBBLESTONE)) {
                return true;
            }
        }
        return path.contains("stone")
                || path.contains("cobble")
                || path.contains("deepslate")
                || path.contains("netherrack")
                || path.contains("blackstone")
                || path.contains("basalt")
                || path.contains("andesite")
                || path.contains("diorite")
                || path.contains("granite")
                || path.contains("tuff");
    }

    private static boolean matches(ResourceLocation id, String name, String query) {
        String key = normalizeQuery(query).toLowerCase(Locale.ROOT);
        if (key.isBlank()) {
            return true;
        }
        String text = (id + " " + name).toLowerCase(Locale.ROOT);
        return text.contains(key) || JustEnoughCharactersCompat.matches(text, key);
    }

    private static String lootName(ResourceLocation id) {
        String path = id == null ? "" : id.getPath();
        if (path.startsWith("chests/")) {
            path = path.substring("chests/".length());
        }
        String[] parts = path.replace('/', ' ').replace('_', ' ').trim().split("\\s+");
        StringBuilder builder = new StringBuilder();
        for (String part : parts) {
            if (part.isBlank()) {
                continue;
            }
            if (builder.length() > 0) {
                builder.append(' ');
            }
            builder.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
        }
        String readable = builder.toString();
        return readable.isBlank() && id != null ? id.toString() : readable;
    }

    public boolean canUse(ServerPlayer player) {
        return SpecialWorkBlockConfig.isHvkAdvancedTreasureCompassEnabled()
                && level != null
                && player.level() == level
                && player.distanceToSqr(worldPosition.getX() + 0.5D, worldPosition.getY() + 0.5D,
                worldPosition.getZ() + 0.5D) <= 64.0D;
    }

    private static String itemKey(Item item) {
        ResourceLocation key = ForgeRegistries.ITEMS.getKey(item);
        return key == null ? item.toString() : key.toString();
    }

    private static String normalizeQuery(String query) {
        return query == null ? "" : query.trim();
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private void playUseSound() {
        if (level != null && !level.isClientSide) {
            level.playSound(null, worldPosition, ModSounds.HVK_ADVANCED_TREASURE_COMPASS_SUCCESS.get(),
                    SoundSource.BLOCKS, 0.85F, 1.0F);
        }
    }

    private record StonePayment(Item item, int count) {
    }
}
