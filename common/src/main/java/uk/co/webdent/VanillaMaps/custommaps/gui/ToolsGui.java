package uk.co.webdent.VanillaMaps.custommaps.gui;

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

public class ToolsGui {

    public enum Tool {
        PENCIL(
                Material.FEATHER,
                "✏ Pencil",
                NamedTextColor.YELLOW,
                List.of(
                        Component.text("Paint individual cells one at a time.", NamedTextColor.GRAY)
                                .decoration(TextDecoration.ITALIC, false),
                        Component.text("Left Click → paint with active colour", NamedTextColor.GRAY)
                                .decoration(TextDecoration.ITALIC, false),
                        Component.text("Right Click → pick colour from canvas", NamedTextColor.GRAY)
                                .decoration(TextDecoration.ITALIC, false))),
        FILL(
                Material.WATER_BUCKET,
                "🪣 Fill (Flood)",
                NamedTextColor.AQUA,
                List.of(
                        Component.text("Flood-fill a connected region of the same colour.", NamedTextColor.GRAY)
                                .decoration(TextDecoration.ITALIC, false),
                        Component.text("Left Click → fill with active colour", NamedTextColor.GRAY)
                                .decoration(TextDecoration.ITALIC, false),
                        Component.text("Right Click → fill with white colour", NamedTextColor.GRAY)
                                .decoration(TextDecoration.ITALIC, false))),
        LINE(
                Material.STRING,
                "📏 Line Tool",
                NamedTextColor.YELLOW,
                List.of(
                        Component.text("Draw lines between two points.", NamedTextColor.GRAY)
                                .decoration(TextDecoration.ITALIC, false),
                        Component.text("Left Click 1 → Set start point", NamedTextColor.GRAY)
                                .decoration(TextDecoration.ITALIC, false),
                        Component.text("Left Click 2 → Draw line", NamedTextColor.GRAY)
                                .decoration(TextDecoration.ITALIC, false),
                        Component.text("Right Click → Cancel selection", NamedTextColor.RED)
                                .decoration(TextDecoration.ITALIC, false))),
        RECTANGLE(
                Material.BLACK_STAINED_GLASS_PANE,
                "□ Rectangle Tool",
                NamedTextColor.DARK_AQUA,
                List.of(
                        Component.text("Draw rectangles between two corners.", NamedTextColor.GRAY)
                                .decoration(TextDecoration.ITALIC, false),
                        Component.text("Left Click 1 → Set first corner", NamedTextColor.GRAY)
                                .decoration(TextDecoration.ITALIC, false),
                        Component.text("Left Click 2 → Draw rectangle", NamedTextColor.GRAY)
                                .decoration(TextDecoration.ITALIC, false),
                        Component.text("Right Click → Cancel selection", NamedTextColor.RED)
                                .decoration(TextDecoration.ITALIC, false))),
        NOISE(
                Material.GRAVEL,
                "🎲 Noise",
                NamedTextColor.LIGHT_PURPLE,
                List.of(
                        Component.text("Fill the entire map with pixel noise.", NamedTextColor.GRAY)
                                .decoration(TextDecoration.ITALIC, false),
                        Component.text("Uses current zoom (Ratio) for noise scale.", NamedTextColor.GRAY)
                                .decoration(TextDecoration.ITALIC, false),
                        Component.text("Reverts to Pencil after use.", NamedTextColor.GOLD)
                                .decoration(TextDecoration.ITALIC, false))),
        CHECKERBOARD(
                Material.DAYLIGHT_DETECTOR,
                "🏁 Checkerboard",
                NamedTextColor.WHITE,
                List.of(
                        Component.text("Tiles a checkerboard pattern across the map.", NamedTextColor.GRAY)
                                .decoration(TextDecoration.ITALIC, false),
                        Component.text("Reverts to Pencil after use.", NamedTextColor.GOLD)
                                .decoration(TextDecoration.ITALIC, false))),
        BRICKS(
                Material.BRICKS,
                "🧱 Bricks",
                NamedTextColor.RED,
                List.of(
                        Component.text("Tiles a brick pattern across the map.", NamedTextColor.GRAY)
                                .decoration(TextDecoration.ITALIC, false),
                        Component.text("Reverts to Pencil after use.", NamedTextColor.GOLD)
                                .decoration(TextDecoration.ITALIC, false))),
        TILES(
                Material.QUARTZ_PILLAR,
                "🟦 Tiles",
                NamedTextColor.BLUE,
                List.of(
                        Component.text("Tiles a grid pattern across the map.", NamedTextColor.GRAY)
                                .decoration(TextDecoration.ITALIC, false),
                        Component.text("Reverts to Pencil after use.", NamedTextColor.GOLD)
                                .decoration(TextDecoration.ITALIC, false)));

        private final Material material;
        private final String displayName;
        private final NamedTextColor nameColor;
        private final List<Component> lore;

        Tool(Material material, String displayName, NamedTextColor nameColor, List<Component> lore) {
            this.material = material;
            this.displayName = displayName;
            this.nameColor = nameColor;
            this.lore = lore;
        }

        public Material getMaterial() {
            return material;
        }

        public String getDisplayName() {
            return displayName;
        }

        public NamedTextColor getNameColor() {
            return nameColor;
        }

        public List<Component> getLore() {
            return lore;
        }
    }

    private static final int SIZE = 9;

    private final Player player;
    private final Inventory inventory;
    private final DrawingGui parentGui;

    public ToolsGui(Player player, DrawingGui parentGui) {
        this.player = player;
        this.parentGui = parentGui;
        this.inventory = Bukkit.createInventory(
                new ToolsGuiHolder(this),
                SIZE,
                Component.text("Select a Tool"));
        buildTools();
    }

    public void open() {
        player.openInventory(inventory);
    }

    private void buildTools() {
        Tool[] tools = Tool.values();
        Tool activeTool = parentGui.getActiveTool();

        for (int i = 0; i < tools.length; i++) {
            Tool tool = tools[i];
            boolean isActive = tool == activeTool;
            inventory.setItem(i, buildToolItem(tool, isActive));
        }
    }

    private ItemStack buildToolItem(Tool tool, boolean isActive) {
        ItemStack item = new ItemStack(tool.getMaterial());
        ItemMeta meta = item.getItemMeta();
        if (meta == null)
            return item;

        // Name — gold + ★ prefix when active
        Component name = isActive
                ? Component.text("★ " + tool.getDisplayName() + " (Active)", NamedTextColor.GOLD)
                        .decoration(TextDecoration.ITALIC, false)
                : Component.text(tool.getDisplayName(), tool.getNameColor())
                        .decoration(TextDecoration.ITALIC, false);
        meta.displayName(name);

        // Lore — copy tool lore, append selection hint
        List<Component> lore = new java.util.ArrayList<>(tool.getLore());
        lore.add(Component.empty());
        lore.add(isActive
                ? Component.text("✔ Currently selected", NamedTextColor.GREEN)
                        .decoration(TextDecoration.ITALIC, false)
                : Component.text("Click to select", NamedTextColor.DARK_GRAY)
                        .decoration(TextDecoration.ITALIC, false));

        meta.lore(lore);
        item.setItemMeta(meta);
        return item;
    }

    public DrawingGui getParentGui() {
        return parentGui;
    }

    public Inventory getInventory() {
        return inventory;
    }
}
