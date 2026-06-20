package com.rzy.dealt_force_skills.client.screen;

import com.rzy.dealt_force_skills.network.C2S_BuyGhrothArmoryItem;
import com.rzy.dealt_force_skills.network.NetworkHandler;
import com.rzy.dealt_force_skills.shop.GhrothArmoryCatalog;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.List;

public class GhrothArmoryScreen extends Screen {
    private static final int PADDING = 12;
    private static final int HEADER_HEIGHT = 58;
    private static final int ROW_HEIGHT = 34;
    private static final int ROW_GAP = 4;
    private static final int SCROLL_STEP = 30;
    private static final List<String> CATEGORY_ORDER = List.of(
            "helmet",
            "armor",
            "medicine",
            "injection_repair",
            "tacz_guns",
            "tacz_attachments",
            "tacz_ammo"
    );

    private static long coins;
    private static List<GhrothArmoryCatalog.Entry> entries = List.of();

    private String category = "helmet";
    private int scrollY;

    public GhrothArmoryScreen(long coins, List<GhrothArmoryCatalog.Entry> entries) {
        super(Component.translatable("screen.dealt_force_skills.ghroth_armory.title"));
        GhrothArmoryScreen.coins = Math.max(0L, coins);
        GhrothArmoryScreen.entries = entries == null ? List.of() : List.copyOf(entries);
        this.category = firstAvailableCategory();
    }

    public static void open(long coins, List<GhrothArmoryCatalog.Entry> entries) {
        Minecraft.getInstance().setScreen(new GhrothArmoryScreen(coins, entries));
    }

    public static void updateCoins(long coins) {
        GhrothArmoryScreen.coins = Math.max(0L, coins);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        int left = panelLeft();
        int top = panelTop();
        graphics.fill(left, top, left + panelWidth(), top + panelHeight(), 0xE00F1115);
        graphics.fill(left, top, left + panelWidth(), top + 1, 0xFFC9A86A);
        graphics.drawString(font, Component.translatable("screen.dealt_force_skills.shop.coins", coins),
                left + PADDING, top + 10, 0xFFC9A86A);
        graphics.drawCenteredString(font, title, width / 2, top + 10, 0xFFFFFF);
        drawTabs(graphics, mouseX, mouseY);
        drawItems(graphics, mouseX, mouseY);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderHoveredTooltip(graphics, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            String tab = tabAt(mouseX, mouseY);
            if (tab != null) {
                category = tab;
                scrollY = 0;
                return true;
            }
            GhrothArmoryCatalog.Entry entry = buyButtonAt(mouseX, mouseY);
            if (entry != null) {
                NetworkHandler.sendToServer(new C2S_BuyGhrothArmoryItem(entry.id()));
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (inside(mouseX, mouseY, contentLeft(), contentTop(), contentWidth(), contentHeight())) {
            int maxScroll = maxScroll();
            if (maxScroll > 0) {
                scrollY = clamp(scrollY - (int) Math.round(delta * SCROLL_STEP), 0, maxScroll);
                return true;
            }
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    private void drawTabs(GuiGraphics graphics, int mouseX, int mouseY) {
        int x = panelLeft() + PADDING;
        int y = panelTop() + 34;
        int gap = 3;
        int tabWidth = Math.max(34, (panelWidth() - PADDING * 2 - gap * (CATEGORY_ORDER.size() - 1)) / CATEGORY_ORDER.size());
        for (int i = 0; i < CATEGORY_ORDER.size(); i++) {
            String tab = CATEGORY_ORDER.get(i);
            int tabX = x + i * (tabWidth + gap);
            boolean selected = tab.equals(category);
            boolean hovered = inside(mouseX, mouseY, tabX, y, tabWidth, 18);
            int fill = selected ? 0xCC4B3D28 : hovered ? 0xAA2A343E : 0x8819232C;
            int edge = selected ? 0xFFC9A86A : hovered ? 0xFF7D8791 : 0x66304050;
            graphics.fill(tabX, y, tabX + tabWidth, y + 18, fill);
            graphics.fill(tabX, y, tabX + tabWidth, y + 1, edge);
            graphics.drawCenteredString(font, fit(Component.translatable("screen.dealt_force_skills.ghroth_armory.category." + tab), tabWidth - 4),
                    tabX + tabWidth / 2, y + 5, selected ? 0xFFFFFF : 0xC6D0DA);
        }
    }

    private void drawItems(GuiGraphics graphics, int mouseX, int mouseY) {
        int left = contentLeft();
        int top = contentTop();
        int width = contentWidth();
        int height = contentHeight();
        graphics.fill(left - 1, top - 1, left + width + 1, top + height + 1, 0x5523333F);
        graphics.enableScissor(left, top, left + width, top + height);
        int y = top - scrollY;
        for (GhrothArmoryCatalog.Entry entry : filteredEntries()) {
            drawEntry(graphics, entry, left, y, width, mouseX, mouseY);
            y += ROW_HEIGHT + ROW_GAP;
        }
        graphics.disableScissor();
        drawScrollbar(graphics);
    }

    private void drawEntry(GuiGraphics graphics, GhrothArmoryCatalog.Entry entry, int x, int y,
                           int rowWidth, int mouseX, int mouseY) {
        if (y + ROW_HEIGHT < contentTop() || y > contentTop() + contentHeight()) {
            return;
        }
        boolean hovered = inside(mouseX, mouseY, x, y, rowWidth, ROW_HEIGHT);
        graphics.fill(x, y, x + rowWidth, y + ROW_HEIGHT, hovered ? 0xAA24313D : 0x8818242F);
        graphics.fill(x, y, x + rowWidth, y + 1, hovered ? 0xFF7D8791 : 0x66304050);
        graphics.renderItem(entry.preview(), x + 5, y + 8);
        int buyWidth = 58;
        int buyX = x + rowWidth - buyWidth - 8;
        int buyY = y + 7;
        graphics.drawString(font, fit(entry.preview().getHoverName(), Math.max(40, buyX - x - 32)), x + 28, y + 7, 0xFFFFFF);
        graphics.drawString(font, Component.translatable("screen.dealt_force_skills.shop.price", entry.price()),
                x + 28, y + 20, coins >= entry.price() ? 0xFFC9A86A : 0xFFDD7777);
        graphics.fill(buyX, buyY, buyX + buyWidth, buyY + 20, coins >= entry.price() ? 0xCC4A3C2A : 0xAA4A3131);
        graphics.drawCenteredString(font, Component.translatable("screen.dealt_force_skills.shop.buy"),
                buyX + buyWidth / 2, buyY + 6, 0xFFFFFF);
    }

    private void drawScrollbar(GuiGraphics graphics) {
        int maxScroll = maxScroll();
        if (maxScroll <= 0) {
            return;
        }
        int x = contentLeft() + contentWidth() - 3;
        int y = contentTop();
        int height = contentHeight();
        graphics.fill(x, y, x + 2, y + height, 0x55304050);
        int fullHeight = contentFullHeight();
        int thumbHeight = Math.max(12, height * height / Math.max(height, fullHeight));
        int thumbTravel = Math.max(1, height - thumbHeight);
        int thumbY = y + (int) Math.round(thumbTravel * (scrollY / (double) maxScroll));
        graphics.fill(x, thumbY, x + 2, thumbY + thumbHeight, 0xAAC9A86A);
    }

    private void renderHoveredTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
        GhrothArmoryCatalog.Entry entry = rowAt(mouseX, mouseY);
        if (entry != null) {
            graphics.renderTooltip(font, entry.preview(), mouseX, mouseY);
        }
    }

    private String tabAt(double mouseX, double mouseY) {
        int x = panelLeft() + PADDING;
        int y = panelTop() + 34;
        int gap = 3;
        int tabWidth = Math.max(34, (panelWidth() - PADDING * 2 - gap * (CATEGORY_ORDER.size() - 1)) / CATEGORY_ORDER.size());
        for (int i = 0; i < CATEGORY_ORDER.size(); i++) {
            int tabX = x + i * (tabWidth + gap);
            if (inside(mouseX, mouseY, tabX, y, tabWidth, 18)) {
                return CATEGORY_ORDER.get(i);
            }
        }
        return null;
    }

    private GhrothArmoryCatalog.Entry buyButtonAt(double mouseX, double mouseY) {
        GhrothArmoryCatalog.Entry entry = rowAt(mouseX, mouseY);
        if (entry == null) {
            return null;
        }
        int rowY = rowY(entry);
        int buyWidth = 58;
        int buyX = contentLeft() + contentWidth() - buyWidth - 8;
        int buyY = rowY + 7;
        return inside(mouseX, mouseY, buyX, buyY, buyWidth, 20) ? entry : null;
    }

    private GhrothArmoryCatalog.Entry rowAt(double mouseX, double mouseY) {
        if (!inside(mouseX, mouseY, contentLeft(), contentTop(), contentWidth(), contentHeight())) {
            return null;
        }
        int y = contentTop() - scrollY;
        for (GhrothArmoryCatalog.Entry entry : filteredEntries()) {
            if (inside(mouseX, mouseY, contentLeft(), y, contentWidth(), ROW_HEIGHT)) {
                return entry;
            }
            y += ROW_HEIGHT + ROW_GAP;
        }
        return null;
    }

    private int rowY(GhrothArmoryCatalog.Entry wanted) {
        int y = contentTop() - scrollY;
        for (GhrothArmoryCatalog.Entry entry : filteredEntries()) {
            if (entry == wanted) {
                return y;
            }
            y += ROW_HEIGHT + ROW_GAP;
        }
        return -1000;
    }

    private List<GhrothArmoryCatalog.Entry> filteredEntries() {
        return entries.stream().filter(entry -> category.equals(entry.categoryKey())).toList();
    }

    private String firstAvailableCategory() {
        return CATEGORY_ORDER.stream()
                .filter(key -> entries.stream().anyMatch(entry -> key.equals(entry.categoryKey())))
                .findFirst()
                .orElse(CATEGORY_ORDER.get(0));
    }

    private int contentFullHeight() {
        int size = filteredEntries().size();
        return Math.max(0, size * (ROW_HEIGHT + ROW_GAP) - ROW_GAP);
    }

    private int maxScroll() {
        return Math.max(0, contentFullHeight() - contentHeight());
    }

    private Component fit(Component component, int maxWidth) {
        String text = component.getString();
        if (font.width(text) <= maxWidth) {
            return component;
        }
        return Component.literal(font.plainSubstrByWidth(text, Math.max(8, maxWidth - font.width("..."))) + "...");
    }

    private int panelWidth() {
        return Math.max(1, Math.min(680, width - 24));
    }

    private int panelHeight() {
        return Math.max(1, Math.min(420, height - 24));
    }

    private int panelLeft() {
        return (width - panelWidth()) / 2;
    }

    private int panelTop() {
        return Math.max(8, (height - panelHeight()) / 2);
    }

    private int contentLeft() {
        return panelLeft() + PADDING;
    }

    private int contentTop() {
        return panelTop() + HEADER_HEIGHT;
    }

    private int contentWidth() {
        return Math.max(1, panelWidth() - PADDING * 2);
    }

    private int contentHeight() {
        return Math.max(20, panelHeight() - HEADER_HEIGHT - PADDING);
    }

    private boolean inside(double mouseX, double mouseY, int x, int y, int w, int h) {
        return mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h;
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}
