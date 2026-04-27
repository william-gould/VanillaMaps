package uk.co.webdent.vanillamaps.paper.custommaps.gui;

import uk.co.webdent.vanillamaps.feature.custommaps.DrawingSession;
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

public class ToolsGuiView {

    private final Player player;
    private final Inventory inventory;
    private final DrawingGuiView parentGui;

    public ToolsGuiView(Player player, DrawingGuiView parentGui) {
        this.player = player;
        this.parentGui = parentGui;
        this.inventory = Bukkit.createInventory(
                new ToolsGuiHolder(this),
                9,
                Component.text("Select a Tool"));
        buildTools();
    }

    public void open() {
        player.openInventory(inventory);
    }

    private void buildTools() {
        DrawingSession.Tool[] tools = DrawingSession.Tool.values();
        DrawingSession.Tool activeTool = parentGui.getSession().getActiveTool();

        for (int i = 0; i < tools.length; i++) {
            DrawingSession.Tool tool = tools[i];
            boolean isActive = tool == activeTool;
            inventory.setItem(i, buildToolItem(tool, isActive));
        }
    }

    private ItemStack buildToolItem(DrawingSession.Tool tool, boolean isActive) {
        Material mat = switch (tool) {
            case PENCIL -> Material.FEATHER;
            case FILL -> Material.WATER_BUCKET;
            case LINE -> Material.STRING;
            case RECTANGLE -> Material.BLACK_STAINED_GLASS_PANE;
            case NOISE -> Material.GRAVEL;
            case CHECKERBOARD -> Material.DAYLIGHT_DETECTOR;
            case BRICKS -> Material.BRICKS;
            case TILES -> Material.QUARTZ_PILLAR;
        };

        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;

        Component name = isActive
                ? Component.text("★ " + tool.name() + " (Active)", NamedTextColor.GOLD).decoration(TextDecoration.ITALIC, false)
                : Component.text(tool.name(), NamedTextColor.YELLOW).decoration(TextDecoration.ITALIC, false);
        meta.displayName(name);

        List<Component> lore = new java.util.ArrayList<>();
        lore.add(Component.empty());
        lore.add(isActive
                ? Component.text("✔ Currently selected", NamedTextColor.GREEN).decoration(TextDecoration.ITALIC, false)
                : Component.text("Click to select", NamedTextColor.DARK_GRAY).decoration(TextDecoration.ITALIC, false));

        meta.lore(lore);
        item.setItemMeta(meta);
        return item;
    }

    public DrawingGuiView getParentGui() {
        return parentGui;
    }

    public Inventory getInventory() {
        return inventory;
    }
}
