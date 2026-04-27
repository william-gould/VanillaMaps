package uk.co.webdent.vanillamaps.fabric.custommaps.gui;

import uk.co.webdent.vanillamaps.fabric.VanillaMapsMod;
import uk.co.webdent.vanillamaps.feature.custommaps.DrawingSession;
import uk.co.webdent.vanillamaps.feature.custommaps.MapCommandLogic;
import uk.co.webdent.vanillamaps.util.BackgroundGenerator;
import uk.co.webdent.vanillamaps.util.MapColorPalette;
import uk.co.webdent.vanillamaps.fabric.platform.FabricGuiBuilder;
import uk.co.webdent.vanillamaps.fabric.platform.FabricItemUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MapItem;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.core.registries.BuiltInRegistries;

public class DrawingGuiView {

    private final ServerPlayer player;
    private final DrawingSession session;
    private FabricGuiBuilder.FabricGui gui;

    public DrawingGuiView(ServerPlayer player, DrawingSession session) {
        this.player = player;
        this.session = session;
    }

    public void open() {
        gui = FabricGuiBuilder.create(6, Component.literal("Draw Your Map"));
        buildCanvas();
        buildActionRow();
        gui.open(player);
    }

    public void refresh() {
        if (gui == null) return;
        gui.clearClickHandlers();
        buildCanvas();
        buildActionRow();
    }

    public DrawingSession getSession() {
        return session;
    }

    private void buildCanvas() {
        int gridWidth = 128 / session.getRatio();
        int gridHeight = 128 / session.getRatio();
        for (int i = 0; i < 45; i++) {
            int col = i % 9;
            int row = i / 9;
            int blockX = col + session.getOffsetX();
            int blockY = row + session.getOffsetY();

            if (blockX == session.getFirstPointX() && blockY == session.getFirstPointY()) {
                ItemStack marker = new ItemStack(Items.LIME_WOOL);
                marker.set(DataComponents.CUSTOM_NAME,
                        Component.literal("POINT 1").withStyle(s -> s.withColor(0x55FF55).withBold(true).withItalic(false)));
                final int slot = i;
                gui.setItem(slot, marker, (p, click) -> handleCanvasClick(slot, click));
            } else if (blockX < gridWidth && blockY < gridHeight) {
                byte color = session.getPixelColorAtSlot(i);
                String woolName = uk.co.webdent.vanillamaps.util.MapColorPalette.getWoolName(color);
                ItemStack item = FabricItemUtils.woolNameToItem(woolName);
                int px = blockX * session.getRatio();
                int py = blockY * session.getRatio();
                item.set(DataComponents.CUSTOM_NAME,
                        Component.literal("X: " + px + ", Y: " + py).withStyle(s -> s.withColor(0xAAAAAA).withItalic(false)));
                final int slot = i;
                gui.setItem(slot, item, (p, click) -> handleCanvasClick(slot, click));
            } else {
                gui.setItem(i, ItemStack.EMPTY);
            }
        }
    }

    private void buildActionRow() {
        gui.setItem(45, named(Items.MAGENTA_GLAZED_TERRACOTTA, "⬆ Move Up"), (p, c) -> { session.pan(0, c == ClickType.QUICK_MOVE ? -5 : -1); refresh(); });
        gui.setItem(46, named(Items.MAGENTA_GLAZED_TERRACOTTA, "⬅ Move Left"), (p, c) -> { session.pan(c == ClickType.QUICK_MOVE ? -9 : -1, 0); refresh(); });
        gui.setItem(47, named(Items.MAGENTA_GLAZED_TERRACOTTA, "➡ Move Right"), (p, c) -> { session.pan(c == ClickType.QUICK_MOVE ? 9 : 1, 0); refresh(); });
        gui.setItem(48, named(Items.MAGENTA_GLAZED_TERRACOTTA, "⬇ Move Down"), (p, c) -> { session.pan(0, c == ClickType.QUICK_MOVE ? 5 : 1); refresh(); });

        ItemStack toolsItem = new ItemStack(Items.CHEST);
        toolsItem.set(DataComponents.CUSTOM_NAME,
                Component.literal("🧰 Tools [" + session.getActiveTool().name() + "]")
                        .withStyle(s -> s.withColor(0xFF55FF).withItalic(false)));
        gui.setItem(49, toolsItem, (p, click) -> {
            if (click == ClickType.PICKUP) {
                new ToolsGuiView(player, this).open();
            } else {
                session.cycleNextTool();
                refresh();
            }
        });

        String activeWoolName = uk.co.webdent.vanillamaps.util.MapColorPalette.getWoolName(session.getActiveColor());
        ItemStack colorItem = FabricItemUtils.woolNameToItem(activeWoolName);
        colorItem.set(DataComponents.CUSTOM_NAME,
                Component.literal("✏ Selected Colour (Click to Change)")
                        .withStyle(s -> s.withColor(0xFFAA00).withItalic(false)));
        gui.setItem(50, colorItem, (p, c) -> new ColorPickerGuiView(player, this).open());

        gui.setItem(51, named(Items.ARROW, "↩ Undo", 0xFFFF55), (p, c) -> { session.undo(); refresh(); });
        gui.setItem(52, named(Items.RED_DYE, "✖ Cancel", 0xFF5555), (p, c) -> p.closeContainer());
        gui.setItem(53, named(Items.GREEN_DYE, "✔ Save Map", 0x55FF55), (p, c) -> {
            ItemStack mainHand = player.getMainHandItem();
            if (mainHand.getItem() instanceof MapItem) {
                MapId mapId = mainHand.get(DataComponents.MAP_ID);
                int mapIdInt = (mapId != null) ? mapId.id() : -1;
                MapCommandLogic logic = VanillaMapsMod.getInstance().getCore().getCustomMapsModule().getMapCommandLogic();
                logic.onSave(player.getUUID(), mapIdInt, session);
            } else {
                player.sendSystemMessage(Component.literal("§cYou must hold a filled map in your main hand to save!"));
            }
            p.closeContainer();
        });
    }

    private void handleCanvasClick(int slot, ClickType click) {
        switch (session.getActiveTool()) {
            case PENCIL -> {
                if (click == ClickType.PICKUP) { session.setPixelAtSlot(slot, session.getActiveColor()); }
                else if (click == ClickType.QUICK_MOVE) { session.setActiveColor(session.getPixelColorAtSlot(slot)); }
                refresh();
            }
            case FILL -> {
                if (click == ClickType.PICKUP) { session.fillPixelAtSlot(slot, session.getActiveColor()); }
                else if (click == ClickType.QUICK_MOVE) { session.fillPixelAtSlot(slot, MapColorPalette.getMapColor("white_wool")); }
                refresh();
            }
            case NOISE -> { session.applyBackground(BackgroundGenerator.BackgroundType.NOISE); refresh(); }
            case CHECKERBOARD -> { session.applyBackground(BackgroundGenerator.BackgroundType.CHECKERBOARD); refresh(); }
            case BRICKS -> { session.applyBackground(BackgroundGenerator.BackgroundType.BRICKS); refresh(); }
            case TILES -> { session.applyBackground(BackgroundGenerator.BackgroundType.TILES); refresh(); }
            case LINE -> {
                if (click == ClickType.QUICK_MOVE) { session.clearLineFirstPoint(); refresh(); }
                else if (click == ClickType.PICKUP) {
                    if (session.getFirstPointX() == -1) { session.setLineFirstPoint(slot); }
                    else { session.drawLineToSlot(slot, session.getActiveColor()); session.clearLineFirstPoint(); }
                    refresh();
                }
            }
            case RECTANGLE -> {
                if (click == ClickType.QUICK_MOVE) { session.clearLineFirstPoint(); refresh(); }
                else if (click == ClickType.PICKUP) {
                    if (session.getFirstPointX() == -1) { session.setLineFirstPoint(slot); }
                    else { session.drawRectToSlot(slot, session.getActiveColor()); session.clearLineFirstPoint(); }
                    refresh();
                }
            }
        }
    }

    private ItemStack named(net.minecraft.world.level.ItemLike item, String name) {
        return named(item, name, 0xFFFFFF);
    }

    private ItemStack named(net.minecraft.world.level.ItemLike item, String name, int color) {
        ItemStack stack = new ItemStack(item);
        stack.set(DataComponents.CUSTOM_NAME,
                Component.literal(name).withStyle(s -> s.withColor(color).withItalic(false)));
        return stack;
    }
}
