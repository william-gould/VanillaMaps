package uk.co.webdent.VanillaMaps.custommaps.gui;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

public class DrawingGuiHolder implements InventoryHolder {
    private final DrawingGui gui;

    public DrawingGuiHolder(DrawingGui gui) {
        this.gui = gui;
    }

    public DrawingGui getGui() {
        return gui;
    }

    @Override
    public @NotNull Inventory getInventory() {
        return gui.getInventory();
    }
}
