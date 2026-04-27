package uk.co.webdent.vanillamaps.paper.custommaps.gui;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

public class ToolsGuiHolder implements InventoryHolder {
    private final ToolsGuiView gui;

    public ToolsGuiHolder(ToolsGuiView gui) {
        this.gui = gui;
    }

    @Override
    public @NotNull Inventory getInventory() {
        return gui.getInventory();
    }

    public ToolsGuiView getGui() {
        return gui;
    }
}
