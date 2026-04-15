package uk.co.webdent.VanillaMaps.custommaps.gui;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

public class ColorPickerGuiHolder implements InventoryHolder {
    private final ColorPickerGui gui;

    public ColorPickerGuiHolder(ColorPickerGui gui) {
        this.gui = gui;
    }

    public ColorPickerGui getGui() {
        return gui;
    }

    @Override
    public @NotNull Inventory getInventory() {
        return gui.getInventory();
    }
}
