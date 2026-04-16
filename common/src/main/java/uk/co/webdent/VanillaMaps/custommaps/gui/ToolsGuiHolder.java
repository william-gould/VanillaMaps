package uk.co.webdent.VanillaMaps.custommaps.gui;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

public class ToolsGuiHolder implements InventoryHolder {

    private final ToolsGui gui;

    public ToolsGuiHolder(ToolsGui gui) {
        this.gui = gui;
    }

    public ToolsGui getGui() {
        return gui;
    }

    @Override
    public @NotNull Inventory getInventory() {
        return gui.getInventory();
    }
}
