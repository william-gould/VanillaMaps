package uk.co.webdent.vanillamaps.paper.custommaps.gui;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

public class ColorPickerGuiHolder implements InventoryHolder {
    private final ColorPickerGuiView gui;

    public ColorPickerGuiHolder(ColorPickerGuiView gui) {
        this.gui = gui;
    }

    @Override
    public @NotNull Inventory getInventory() {
        return gui.getInventory();
    }

    public ColorPickerGuiView getGui() {
        return gui;
    }
}
