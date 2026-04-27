package uk.co.webdent.vanillamaps.paper.custommaps.gui;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

public class DrawingGuiHolder implements InventoryHolder {
    private final DrawingGuiView gui;

    public DrawingGuiHolder(DrawingGuiView gui) {
        this.gui = gui;
    }

    @Override
    public @NotNull Inventory getInventory() {
        return gui.getInventory();
    }

    public DrawingGuiView getGui() {
        return gui;
    }
}
