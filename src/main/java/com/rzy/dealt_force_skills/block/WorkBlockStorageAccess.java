package com.rzy.dealt_force_skills.block;

import com.rzy.dealt_force_skills.compat.HvkLogisticsBridge;
import com.rzy.dealt_force_skills.event.CommonEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.IItemHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public final class WorkBlockStorageAccess {
    private static final double OWNER_INVENTORY_RANGE = 12.0D;

    private WorkBlockStorageAccess() {
    }

    public static List<SourceSlot> collectSources(Level level, BlockPos pos, ServerPlayer player) {
        List<SourceSlot> sources = new ArrayList<>();
        if (level == null || pos == null || player == null) {
            return sources;
        }
        if (player.level() == level
                && player.distanceToSqr(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D)
                <= OWNER_INVENTORY_RANGE * OWNER_INVENTORY_RANGE) {
            Inventory inventory = player.getInventory();
            for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
                sources.add(new PlayerSourceSlot(inventory, slot));
            }
        }
        for (Direction direction : Direction.values()) {
            BlockEntity neighbor = level.getBlockEntity(pos.relative(direction));
            if (neighbor == null) {
                continue;
            }
            neighbor.getCapability(ForgeCapabilities.ITEM_HANDLER, direction.getOpposite())
                    .ifPresent(handler -> addHandlerSources(sources, handler));
        }
        HvkLogisticsBridge.collectNetworkItemHandlers(level, pos, handler -> addHandlerSources(sources, handler));
        return sources;
    }

    private static void addHandlerSources(List<SourceSlot> sources, IItemHandler handler) {
        for (int slot = 0; slot < handler.getSlots(); slot++) {
            sources.add(new HandlerSourceSlot(handler, slot));
        }
    }

    public static Map<Item, Integer> countItems(List<SourceSlot> sources) {
        Map<Item, Integer> counts = new HashMap<>();
        for (SourceSlot source : sources) {
            ItemStack stack = source.stack();
            if (!stack.isEmpty()) {
                counts.merge(stack.getItem(), stack.getCount(), Integer::sum);
            }
        }
        return counts;
    }

    public static int countItem(List<SourceSlot> sources, Item item) {
        int count = 0;
        for (SourceSlot source : sources) {
            ItemStack stack = source.stack();
            if (!stack.isEmpty() && stack.getItem() == item) {
                count += stack.getCount();
            }
        }
        return count;
    }

    public static int countMatching(List<SourceSlot> sources, Predicate<ItemStack> predicate) {
        int count = 0;
        for (SourceSlot source : sources) {
            ItemStack stack = source.stack();
            if (!stack.isEmpty() && predicate.test(stack)) {
                count += stack.getCount();
            }
        }
        return count;
    }

    public static int consumeItem(List<SourceSlot> sources, Item item, int count) {
        return consumeMatching(sources, stack -> stack.getItem() == item, count);
    }

    public static int consumeMatching(List<SourceSlot> sources, Predicate<ItemStack> predicate, int count) {
        int remaining = Math.max(0, count);
        for (SourceSlot source : sources) {
            if (remaining <= 0) {
                break;
            }
            remaining -= source.extract(predicate, remaining);
        }
        return count - remaining;
    }

    public static void giveOrDrop(Player player, ItemStack stack) {
        if (player == null || stack == null || stack.isEmpty()) {
            return;
        }
        boolean added = player.getInventory().add(stack);
        if ((!added || !stack.isEmpty()) && !stack.isEmpty()) {
            player.drop(stack, false);
        }
    }

    public static void giveWithBlockLootBonus(Player player, Level level, BlockPos pos, ItemStack stack) {
        if (player == null || stack == null || stack.isEmpty()) {
            return;
        }
        int remaining = stack.getCount();
        while (remaining > 0) {
            ItemStack split = stack.copy();
            split.setCount(Math.min(split.getMaxStackSize(), remaining));
            remaining -= split.getCount();
            ItemStack bonusBase = split.copy();
            giveOrDrop(player, split);
            CommonEvents.grantHvkBlockLootBonuses(player, level, pos, bonusBase);
        }
    }

    public interface SourceSlot {
        ItemStack stack();

        int extract(Predicate<ItemStack> predicate, int max);
    }

    private static final class PlayerSourceSlot implements SourceSlot {
        private final Inventory inventory;
        private final int slot;

        private PlayerSourceSlot(Inventory inventory, int slot) {
            this.inventory = inventory;
            this.slot = slot;
        }

        @Override
        public ItemStack stack() {
            return inventory.getItem(slot);
        }

        @Override
        public int extract(Predicate<ItemStack> predicate, int max) {
            ItemStack stack = inventory.getItem(slot);
            if (stack.isEmpty() || !predicate.test(stack)) {
                return 0;
            }
            int removed = Math.min(max, stack.getCount());
            stack.shrink(removed);
            inventory.setChanged();
            return removed;
        }
    }

    private static final class HandlerSourceSlot implements SourceSlot {
        private final IItemHandler handler;
        private final int slot;

        private HandlerSourceSlot(IItemHandler handler, int slot) {
            this.handler = handler;
            this.slot = slot;
        }

        @Override
        public ItemStack stack() {
            return handler.getStackInSlot(slot);
        }

        @Override
        public int extract(Predicate<ItemStack> predicate, int max) {
            ItemStack stack = handler.getStackInSlot(slot);
            if (stack.isEmpty() || !predicate.test(stack)) {
                return 0;
            }
            ItemStack extracted = handler.extractItem(slot, max, false);
            return !extracted.isEmpty() && predicate.test(extracted) ? extracted.getCount() : 0;
        }
    }
}
