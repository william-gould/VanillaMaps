package uk.co.webdent.VanillaMaps.custommaps.gui;

import uk.co.webdent.VanillaMaps.custommaps.util.ColorPalette;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class ColorPickerGui {
    private final Player player;
    private final Inventory inventory;
    private final DrawingGui parentGui;

    public ColorPickerGui(Player player, DrawingGui parentGui) {
        this.player = player;
        this.parentGui = parentGui;

        int size = ((ColorPalette.ALL_COLORS.size() / 9) + 1) * 9;
        this.inventory = Bukkit.createInventory(new ColorPickerGuiHolder(this), size,
                Component.text("Select a Colour"));

        buildColors();
    }

    public void open() {
        player.openInventory(inventory);
    }

    private void buildColors() {
        for (int i = 0; i < ColorPalette.ALL_COLORS.size(); i++) {
            inventory.setItem(i, new ItemStack(ColorPalette.ALL_COLORS.get(i)));
        }
    }

    public DrawingGui getParentGui() {
        return parentGui;
    }

    public Inventory getInventory() {
        return inventory;
    }
}
