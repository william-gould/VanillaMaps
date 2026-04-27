package uk.co.webdent.vanillamaps.paper.custommaps.gui;

import uk.co.webdent.vanillamaps.feature.custommaps.DrawingSession;
import uk.co.webdent.vanillamaps.util.MapColorPalette;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import java.util.List;

public class DrawingGuiView {

    private final Player player;
    private final DrawingSession session;
    private final Inventory inventory;

    private ColorPickerGuiView colorPickerGui;
    private ToolsGuiView toolsGui;

    public DrawingGuiView(Player player, DrawingSession session) {
        this.player = player;
        this.session = session;
        this.inventory = Bukkit.createInventory(new DrawingGuiHolder(this), 54, Component.text("Draw Your Map"));

        buildCanvas();
        buildActionRow();
    }

    public void open() {
        player.openInventory(inventory);
    }

    public DrawingSession getSession() {
        return session;
    }

    public void buildCanvas() {
        int gridWidth = 128 / session.getRatio();
        int gridHeight = 128 / session.getRatio();
        
        for (int i = 0; i < 45; i++) {
            int col = i % 9;
            int row = i / 9;
            int blockX = col + session.getOffsetX();
            int blockY = row + session.getOffsetY();

            if (blockX == session.getFirstPointX() && blockY == session.getFirstPointY()) {
                ItemStack item = new ItemStack(Material.LIME_WOOL);
                ItemMeta meta = item.getItemMeta();
                if (meta != null) {
                    meta.displayName(
                            Component.text("POINT 1", NamedTextColor.GREEN)
                                    .decoration(TextDecoration.BOLD, true)
                                    .decoration(TextDecoration.ITALIC, false));
                    item.setItemMeta(meta);
                }
                inventory.setItem(i, item);
            } else if (blockX < gridWidth && blockY < gridHeight) {
                byte color = session.getPixelColorAtSlot(i);
                setCanvasItem(i, blockX, blockY, color);
            } else {
                inventory.setItem(i, new ItemStack(Material.AIR));
            }
        }
    }

    public void buildActionRow() {
        for (int i = 45; i < 54; i++)
            inventory.setItem(i, null);

        inventory.setItem(45, createNamedItem(Material.MAGENTA_GLAZED_TERRACOTTA, "⬆ Move Up"));
        inventory.setItem(46, createNamedItem(Material.MAGENTA_GLAZED_TERRACOTTA, "⬅ Move Left"));
        inventory.setItem(47, createNamedItem(Material.MAGENTA_GLAZED_TERRACOTTA, "➡ Move Right"));
        inventory.setItem(48, createNamedItem(Material.MAGENTA_GLAZED_TERRACOTTA, "⬇ Move Down"));

        updateToolsButton();
        updateActiveColorDisplay();

        inventory.setItem(51, createNamedItem(Material.ARROW, "↩ Undo", NamedTextColor.YELLOW));
        inventory.setItem(52, createNamedItem(Material.RED_DYE, "✖ Cancel", NamedTextColor.RED));
        inventory.setItem(53, createNamedItem(Material.GREEN_DYE, "✔ Save Map", NamedTextColor.GREEN));
    }

    public void updateToolsButton() {
        ItemStack item = new ItemStack(Material.CHEST);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(
                    Component.text("🧰 Tools", NamedTextColor.LIGHT_PURPLE)
                            .decoration(TextDecoration.ITALIC, false));

            DrawingSession.Tool activeTool = session.getActiveTool();
            meta.lore(List.of(
                    Component.text("Active: ", NamedTextColor.GRAY)
                            .decoration(TextDecoration.ITALIC, false)
                            .append(Component.text(activeTool.name(), NamedTextColor.AQUA)
                                    .decoration(TextDecoration.ITALIC, false)),
                    Component.empty(),
                    Component.text("Left Click → Open tools menu", NamedTextColor.YELLOW)
                            .decoration(TextDecoration.ITALIC, false),
                    Component.text("Right Click → Toggle to next tool", NamedTextColor.YELLOW)
                            .decoration(TextDecoration.ITALIC, false)));
            item.setItemMeta(meta);
        }
        inventory.setItem(49, item);
    }

    private void setCanvasItem(int slot, int blockX, int blockY, byte color) {
        
        String match = "white_wool";
        for (String c : MapColorPalette.ALL_COLOR_NAMES) {
            if (MapColorPalette.getMapColor(c) == color) {
                match = c;
                break;
            }
        }
        Material mat = Material.matchMaterial(match.toUpperCase());
        if (mat == null) mat = Material.WHITE_WOOL;
        
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            int px = blockX * session.getRatio();
            int py = blockY * session.getRatio();
            meta.displayName(Component.text("X: " + px + ", Y: " + py, NamedTextColor.GRAY)
                    .decoration(TextDecoration.ITALIC, false));
            item.setItemMeta(meta);
        }
        inventory.setItem(slot, item);
    }

    private ItemStack createNamedItem(Material mat, String name) {
        return createNamedItem(mat, name, NamedTextColor.WHITE);
    }

    private ItemStack createNamedItem(Material mat, String name, NamedTextColor color) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text(name).color(color)
                    .decoration(TextDecoration.ITALIC, false));
            item.setItemMeta(meta);
        }
        return item;
    }

    public void updateActiveColorDisplay() {
        String match = "white_wool";
        for (String c : MapColorPalette.ALL_COLOR_NAMES) {
            if (MapColorPalette.getMapColor(c) == session.getActiveColor()) {
                match = c;
                break;
            }
        }
        Material mat = Material.matchMaterial(match.toUpperCase());
        if (mat == null) mat = Material.WHITE_WOOL;
        
        ItemStack activeItem = new ItemStack(mat);
        ItemMeta meta = activeItem.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text("✏ Selected Colour (Click to Change)")
                    .color(NamedTextColor.GOLD)
                    .decoration(TextDecoration.ITALIC, false));
            activeItem.setItemMeta(meta);
        }
        inventory.setItem(50, activeItem);
    }

    public ColorPickerGuiView getColorPickerGui() {
        if (colorPickerGui == null) {
            colorPickerGui = new ColorPickerGuiView(player, this);
        }
        return colorPickerGui;
    }

    public ToolsGuiView getToolsGui() {
        if (toolsGui == null) {
            toolsGui = new ToolsGuiView(player, this);
        }
        return toolsGui;
    }

    public void invalidateToolsGui() {
        toolsGui = null;
    }

    public Inventory getInventory() {
        return inventory;
    }
}