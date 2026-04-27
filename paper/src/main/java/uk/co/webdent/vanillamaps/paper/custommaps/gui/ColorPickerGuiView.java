package uk.co.webdent.vanillamaps.paper.custommaps.gui;

import uk.co.webdent.vanillamaps.util.MapColorPalette;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class ColorPickerGuiView {
    private final Player player;
    private final Inventory inventory;
    private final DrawingGuiView parentGui;

    public ColorPickerGuiView(Player player, DrawingGuiView parentGui) {
        this.player = player;
        this.parentGui = parentGui;

        int size = ((MapColorPalette.ALL_COLOR_NAMES.size() / 9) + 1) * 9;
        this.inventory = Bukkit.createInventory(new ColorPickerGuiHolder(this), size,
                Component.text("Select a Colour"));

        buildColors();
    }

    public void open() {
        player.openInventory(inventory);
    }

    private void buildColors() {
        for (int i = 0; i < MapColorPalette.ALL_COLOR_NAMES.size(); i++) {
            String name = MapColorPalette.ALL_COLOR_NAMES.get(i);
            Material mat = Material.matchMaterial(name.toUpperCase());
            if (mat == null) mat = Material.WHITE_WOOL;
            inventory.setItem(i, new ItemStack(mat));
        }
    }

    public DrawingGuiView getParentGui() {
        return parentGui;
    }

    public Inventory getInventory() {
        return inventory;
    }
}
