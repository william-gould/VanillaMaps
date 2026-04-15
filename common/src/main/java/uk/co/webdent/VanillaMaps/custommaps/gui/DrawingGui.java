package uk.co.webdent.VanillaMaps.custommaps.gui;

import uk.co.webdent.VanillaMaps.custommaps.CustomMapsModule;
import uk.co.webdent.VanillaMaps.custommaps.util.ColorPalette;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

public class DrawingGui {
    private final Player player;
    private final Inventory inventory;
    private final byte[] pixels;
    private byte activeColor;

    private final int ratio;
    private int offsetX = 0;
    private int offsetY = 0;
    private boolean fillMode = false;

    public DrawingGui(Player player, int ratio) {
        this(player, ratio, null);
    }

    public DrawingGui(Player player, int ratio, byte[] existingPixels) {
        this.player = player;
        this.ratio = ratio;
        this.inventory = Bukkit.createInventory(new DrawingGuiHolder(this), 54, Component.text("Draw Your Map"));
        this.pixels = new byte[128 * 128];

        String defaultColorName = CustomMapsModule.getInstance().getPlugin().getConfig().getString("default-background",
                "WHITE");
        Material defMat = Material.matchMaterial(defaultColorName + "_WOOL");
        if (defMat == null)
            defMat = Material.WHITE_WOOL;
        this.activeColor = ColorPalette.getMapColor(defMat);

        if (existingPixels != null && existingPixels.length == 128 * 128) {
            System.arraycopy(existingPixels, 0, this.pixels, 0, 128 * 128);
        } else {
            Arrays.fill(this.pixels, this.activeColor);
        }

        buildCanvas();
        buildActionRow();
    }

    public void open() {
        player.openInventory(inventory);
    }

    public void buildCanvas() {
        int gridWidth = 128 / ratio;
        int gridHeight = 128 / ratio;
        for (int i = 0; i < 45; i++) {
            int col = i % 9;
            int row = i / 9;
            int blockX = col + offsetX;
            int blockY = row + offsetY;

            if (blockX < gridWidth && blockY < gridHeight) {
                int px = blockX * ratio;
                int py = blockY * ratio;
                byte color = pixels[py * 128 + px];
                inventory.setItem(i, ColorPalette.getDisplayItem(color));
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

        updateActiveColorDisplay();
        updateFillModeDisplay();

        inventory.setItem(52, createNamedItem(Material.RED_DYE, "✖ Cancel", NamedTextColor.RED));
        inventory.setItem(53, createNamedItem(Material.GREEN_DYE, "✔ Save Map", NamedTextColor.GREEN));
    }

    private ItemStack createNamedItem(Material mat, String name) {
        return createNamedItem(mat, name, NamedTextColor.WHITE);
    }

    private ItemStack createNamedItem(Material mat, String name, NamedTextColor color) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text(name).color(color));
            item.setItemMeta(meta);
        }
        return item;
    }

    public void updateActiveColorDisplay() {
        ItemStack activeItem = ColorPalette.getDisplayItem(activeColor);
        ItemMeta meta = activeItem.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text("✏ Selected Colour (Click to Change)").color(NamedTextColor.GOLD));
            activeItem.setItemMeta(meta);
        }
        inventory.setItem(50, activeItem);
    }

    public void setActiveColor(byte color) {
        this.activeColor = color;
        updateActiveColorDisplay();
    }

    public void updateFillModeDisplay() {
        if (fillMode) {
            inventory.setItem(49, createNamedItem(Material.WATER_BUCKET, "Fill Mode: ON", NamedTextColor.AQUA));
        } else {
            inventory.setItem(49, createNamedItem(Material.BUCKET, "Fill Mode: OFF", NamedTextColor.GRAY));
        }
    }

    public void toggleFillMode() {
        this.fillMode = !this.fillMode;
        updateFillModeDisplay();
    }

    public boolean isFillMode() {
        return fillMode;
    }

    public byte getActiveColor() {
        return activeColor;
    }

    public byte[] getPixelData() {
        return pixels;
    }

    public void setPixel(int slot, byte color) {
        if (slot < 0 || slot >= 45)
            return;
        inventory.setItem(slot, ColorPalette.getDisplayItem(color));

        int col = slot % 9;
        int row = slot / 9;

        int blockX = col + offsetX;
        int blockY = row + offsetY;

        int gridWidth = 128 / ratio;
        int gridHeight = 128 / ratio;

        if (blockX < gridWidth && blockY < gridHeight) {
            for (int subY = 0; subY < ratio; subY++) {
                for (int subX = 0; subX < ratio; subX++) {
                    int px = blockX * ratio + subX;
                    int py = blockY * ratio + subY;
                    pixels[py * 128 + px] = color;
                }
            }
        }
    }

    public void fillPixel(int slot, byte replacementColor) {
        if (slot < 0 || slot >= 45)
            return;

        int col = slot % 9;
        int row = slot / 9;

        int blockX = col + offsetX;
        int blockY = row + offsetY;

        int gridWidth = 128 / ratio;
        int gridHeight = 128 / ratio;

        if (blockX < gridWidth && blockY < gridHeight) {
            int startX = blockX * ratio;
            int startY = blockY * ratio;

            byte targetColor = pixels[startY * 128 + startX];
            if (targetColor == replacementColor)
                return;

            java.util.Queue<int[]> queue = new java.util.LinkedList<>();
            queue.add(new int[] { startX, startY });

            while (!queue.isEmpty()) {
                int[] p = queue.poll();
                int px = p[0];
                int py = p[1];

                int index = py * 128 + px;
                if (pixels[index] == targetColor) {
                    pixels[index] = replacementColor;
                    if (px + 1 < 128)
                        queue.add(new int[] { px + 1, py });
                    if (px - 1 >= 0)
                        queue.add(new int[] { px - 1, py });
                    if (py + 1 < 128)
                        queue.add(new int[] { px, py + 1 });
                    if (py - 1 >= 0)
                        queue.add(new int[] { px, py - 1 });
                }
            }

            buildCanvas();
        }
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
