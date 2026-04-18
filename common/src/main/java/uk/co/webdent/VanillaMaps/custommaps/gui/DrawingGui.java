package uk.co.webdent.VanillaMaps.custommaps.gui;

import uk.co.webdent.VanillaMaps.custommaps.CustomMapsModule;
import uk.co.webdent.VanillaMaps.custommaps.util.ColorPalette;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;

public class DrawingGui {

    private final Player player;
    private final Inventory inventory;
    private final byte[] pixels;
    private byte activeColor;

    private final int maxUndoDepth;
    private int ratio;
    private int offsetX = 0;
    private int offsetY = 0;

    private final byte backgroundColor;
    private ToolsGui.Tool activeTool = ToolsGui.Tool.PENCIL;
    private int firstPointX = -1;
    private int firstPointY = -1;

    private final Deque<byte[]> undoStack = new ArrayDeque<>();

    private ColorPickerGui colorPickerGui;
    private ToolsGui toolsGui;

    public DrawingGui(Player player, int ratio) {
        this(player, ratio, null);
    }

    public DrawingGui(Player player, int ratio, byte[] existingPixels) {
        this.player = player;
        this.ratio = ratio;
        this.inventory = Bukkit.createInventory(new DrawingGuiHolder(this), 54, Component.text("Draw Your Map"));
        this.pixels = new byte[128 * 128];

        String defaultColorName = CustomMapsModule.getInstance().getPlugin().getConfig()
                .getString("default-background", "WHITE");
        Material defMat = Material.matchMaterial(defaultColorName + "_WOOL");
        if (defMat == null)
            defMat = Material.WHITE_WOOL;
        this.backgroundColor = ColorPalette.getMapColor(defMat);
        this.activeColor = this.backgroundColor;
        this.maxUndoDepth = CustomMapsModule.getInstance().getPlugin().getConfig().getInt("undo-depth", 10);

        if (existingPixels != null && existingPixels.length == 128 * 128) {
            System.arraycopy(existingPixels, 0, this.pixels, 0, 128 * 128);
        } else {
            Arrays.fill(this.pixels, this.backgroundColor);
        }

        buildCanvas();
        buildActionRow();
    }

    public void open() {
        player.openInventory(inventory);
    }

    public int getRatio() {
        return ratio;
    }

    public void setRatio(int newRatio) {
        if (newRatio <= 0 || 128 % newRatio != 0)
            throw new IllegalArgumentException("ratio must divide 128 evenly, got: " + newRatio);
        this.ratio = newRatio;
        int gridWidth = 128 / ratio;
        int gridHeight = 128 / ratio;
        offsetX = Math.max(0, Math.min(Math.max(0, gridWidth - 9), offsetX));
        offsetY = Math.max(0, Math.min(Math.max(0, gridHeight - 5), offsetY));
        buildCanvas();
    }

    public void buildCanvas() {
        int gridWidth = 128 / ratio;
        int gridHeight = 128 / ratio;
        for (int i = 0; i < 45; i++) {
            int col = i % 9;
            int row = i / 9;
            int blockX = col + offsetX;
            int blockY = row + offsetY;

            if (blockX == firstPointX && blockY == firstPointY) {
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
                byte color = pixels[blockY * ratio * 128 + blockX * ratio];
                setCanvasItem(i, blockX, blockY, color);
            } else {
                inventory.setItem(i, new ItemStack(Material.AIR));
            }
        }
    }

    private void buildActionRow() {
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

            String toolName = activeTool.getDisplayName();
            meta.lore(List.of(
                    Component.text("Active: ", NamedTextColor.GRAY)
                            .decoration(TextDecoration.ITALIC, false)
                            .append(Component.text(toolName, activeTool.getNameColor())
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

    public ToolsGui.Tool getActiveTool() {
        return activeTool;
    }

    public void setActiveTool(ToolsGui.Tool tool) {
        this.activeTool = tool;
        updateToolsButton();
        invalidateToolsGui();
    }

    public void cycleNextTool() {
        ToolsGui.Tool[] tools = ToolsGui.Tool.values();
        int next = (activeTool.ordinal() + 1) % tools.length;
        setActiveTool(tools[next]);
    }

    public void applyBackground(
            uk.co.webdent.VanillaMaps.custommaps.gui.generator.BackgroundGenerator.BackgroundType type) {
        saveUndoSnapshot();
        uk.co.webdent.VanillaMaps.custommaps.gui.generator.BackgroundGenerator.apply(this, type);
        buildCanvas();
        setActiveTool(ToolsGui.Tool.PENCIL);
    }

    public void saveUndoSnapshot() {
        if (undoStack.size() >= maxUndoDepth) {
            undoStack.pollLast(); // evict oldest
        }
        byte[] snapshot = new byte[pixels.length];
        System.arraycopy(pixels, 0, snapshot, 0, pixels.length);
        undoStack.push(snapshot); // push to head (most recent)
    }

    public void undo() {
        byte[] snapshot = undoStack.poll(); // pop from head
        if (snapshot == null)
            return;
        System.arraycopy(snapshot, 0, pixels, 0, pixels.length);
        buildCanvas();
    }

    public byte getBackgroundColor() {
        return backgroundColor;
    }

    public int getFirstPointX() {
        return firstPointX;
    }

    public void setLineFirstPoint(int slot) {
        int col = slot % 9;
        int row = slot / 9;
        this.firstPointX = col + offsetX;
        this.firstPointY = row + offsetY;
        buildCanvas();
    }

    public void clearLineFirstPoint() {
        this.firstPointX = -1;
        this.firstPointY = -1;
        buildCanvas();
    }

    public void drawLine(int slot2, byte color) {
        if (firstPointX == -1)
            return;
        saveUndoSnapshot();

        int x0 = firstPointX;
        int y0 = firstPointY;

        int col2 = slot2 % 9;
        int row2 = slot2 / 9;
        int x1 = col2 + offsetX;
        int y1 = row2 + offsetY;

        // Bresenham's Line Algorithm
        int dx = Math.abs(x1 - x0);
        int dy = -Math.abs(y1 - y0);
        int sx = x0 < x1 ? 1 : -1;
        int sy = y0 < y1 ? 1 : -1;
        int err = dx + dy;

        while (true) {
            setBlock(x0, y0, color);
            if (x0 == x1 && y0 == y1)
                break;
            int e2 = 2 * err;
            if (e2 >= dy) {
                err += dy;
                x0 += sx;
            }
            if (e2 <= dx) {
                err += dx;
                y0 += sy;
            }
        }
    }

    public void drawRect(int slot2, byte color) {
        if (firstPointX == -1)
            return;
        saveUndoSnapshot();

        int x0 = firstPointX;
        int y0 = firstPointY;

        int col2 = slot2 % 9;
        int row2 = slot2 / 9;
        int x1 = col2 + offsetX;
        int y1 = row2 + offsetY;

        int minX = Math.min(x0, x1);
        int maxX = Math.max(x0, x1);
        int minY = Math.min(y0, y1);
        int maxY = Math.max(y0, y1);

        // Top and bottom edges
        for (int x = minX; x <= maxX; x++) {
            setBlock(x, minY, color);
            setBlock(x, maxY, color);
        }
        // Left and right edges
        for (int y = minY; y <= maxY; y++) {
            setBlock(minX, y, color);
            setBlock(maxX, y, color);
        }
    }

    private void setBlock(int blockX, int blockY, byte color) {
        int gridWidth = 128 / ratio;
        int gridHeight = 128 / ratio;

        if (blockX >= 0 && blockX < gridWidth && blockY >= 0 && blockY < gridHeight) {
            for (int subY = 0; subY < ratio; subY++) {
                for (int subX = 0; subX < ratio; subX++) {
                    int px = blockX * ratio + subX;
                    int py = blockY * ratio + subY;
                    pixels[py * 128 + px] = color;
                }
            }
        }
    }

    private void setCanvasItem(int slot, int blockX, int blockY, byte color) {
        ItemStack item = ColorPalette.getDisplayItem(color);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            int px = blockX * ratio;
            int py = blockY * ratio;
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
        ItemStack activeItem = ColorPalette.getDisplayItem(activeColor);
        ItemMeta meta = activeItem.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text("✏ Selected Colour (Click to Change)")
                    .color(NamedTextColor.GOLD)
                    .decoration(TextDecoration.ITALIC, false));
            activeItem.setItemMeta(meta);
        }
        inventory.setItem(50, activeItem);
    }

    public void setActiveColor(byte color) {
        this.activeColor = color;
        updateActiveColorDisplay();
    }

    public byte getActiveColor() {
        return activeColor;
    }

    public byte[] getPixelData() {
        return pixels;
    }

    public byte getPixelColorAt(int slot) {
        if (slot < 0 || slot >= 45)
            return 0;
        int col = slot % 9;
        int row = slot / 9;
        int blockX = col + offsetX;
        int blockY = row + offsetY;
        int gridWidth = 128 / ratio;
        int gridHeight = 128 / ratio;
        if (blockX < gridWidth && blockY < gridHeight) {
            return pixels[blockY * ratio * 128 + blockX * ratio];
        }
        return 0;
    }

    public void setPixel(int slot, byte color) {
        if (slot < 0 || slot >= 45)
            return;
        saveUndoSnapshot();

        int col = slot % 9;
        int row = slot / 9;
        int blockX = col + offsetX;
        int blockY = row + offsetY;

        setBlock(blockX, blockY, color);
        setCanvasItem(slot, blockX, blockY, color);
    }

    public void fillPixel(int slot, byte replacementColor) {
        if (slot < 0 || slot >= 45)
            return;
        saveUndoSnapshot();

        int col = slot % 9;
        int row = slot / 9;
        int blockX = col + offsetX;
        int blockY = row + offsetY;
        int gridWidth = 128 / ratio;
        int gridHeight = 128 / ratio;

        if (blockX >= gridWidth || blockY >= gridHeight)
            return;

        byte targetColor = pixels[blockY * ratio * 128 + blockX * ratio];
        if (targetColor == replacementColor)
            return;

        ArrayDeque<int[]> queue = new ArrayDeque<>();
        queue.add(new int[] { blockX, blockY });

        while (!queue.isEmpty()) {
            int[] p = queue.poll();
            int bx = p[0];
            int by = p[1];

            if (bx < 0 || bx >= gridWidth || by < 0 || by >= gridHeight)
                continue;
            if (pixels[by * ratio * 128 + bx * ratio] != targetColor)
                continue;

            setBlock(bx, by, replacementColor);

            queue.add(new int[] { bx + 1, by });
            queue.add(new int[] { bx - 1, by });
            queue.add(new int[] { bx, by + 1 });
            queue.add(new int[] { bx, by - 1 });
        }

        buildCanvas();
    }

    public ColorPickerGui getColorPickerGui() {
        if (colorPickerGui == null) {
            colorPickerGui = new ColorPickerGui(player, this);
        }
        return colorPickerGui;
    }

    public ToolsGui getToolsGui() {
        if (toolsGui == null) {
            toolsGui = new ToolsGui(player, this);
        }
        return toolsGui;
    }

    public void invalidateToolsGui() {
        toolsGui = null;
    }

    public void pan(int dx, int dy) {
        int gridWidth = 128 / ratio;
        int gridHeight = 128 / ratio;
        offsetX = Math.max(0, Math.min(Math.max(0, gridWidth - 9), offsetX + dx));
        offsetY = Math.max(0, Math.min(Math.max(0, gridHeight - 5), offsetY + dy));
        buildCanvas();
    }

    public Inventory getInventory() {
        return inventory;
    }
}