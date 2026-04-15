package uk.co.webdent.VanillaMaps.custommaps.gui;

import uk.co.webdent.VanillaMaps.custommaps.CustomMapsModule;
import uk.co.webdent.VanillaMaps.custommaps.util.ColorPalette;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class GuiListener implements Listener {

    private final CustomMapsModule plugin;

    public GuiListener(CustomMapsModule plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player))
            return;

        if (event.getInventory().getHolder() instanceof DrawingGuiHolder holder) {
            event.setCancelled(true);
            DrawingGui gui = holder.getGui();
            int slot = event.getRawSlot();

            if (slot >= 0 && slot < 45) { // Canvas
                if (gui.isFillMode()) {
                    if (event.isLeftClick()) {
                        gui.fillPixel(slot, gui.getActiveColor());
                    } else if (event.isRightClick()) {
                        gui.fillPixel(slot, ColorPalette.getMapColor(Material.WHITE_WOOL));
                    }
                } else {
                    if (event.isLeftClick()) {
                        gui.setPixel(slot, gui.getActiveColor());
                    } else if (event.isRightClick()) {
                        gui.setPixel(slot, ColorPalette.getMapColor(Material.WHITE_WOOL));
                    }
                }
            } else if (slot == 45) { // Up
                gui.pan(0, event.isShiftClick() ? -5 : -1);
            } else if (slot == 46) { // Left
                gui.pan(event.isShiftClick() ? -9 : -1, 0);
            } else if (slot == 47) { // Right
                gui.pan(event.isShiftClick() ? 9 : 1, 0);
            } else if (slot == 48) { // Down
                gui.pan(0, event.isShiftClick() ? 5 : 1);
            } else if (slot == 49) { // Toggle Fill Mode
                gui.toggleFillMode();
            } else if (slot == 50) { // Active Color -> Open Picker
                ColorPickerGui picker = new ColorPickerGui(player, gui);
                picker.open();
            } else if (slot == 52) { // Cancel
                player.closeInventory();
            } else if (slot == 53) { // Save
                plugin.getMapDataStore().save(player, gui);
                player.closeInventory();
            }
        } else if (event.getInventory().getHolder() instanceof ColorPickerGuiHolder holder) {
            event.setCancelled(true);
            ColorPickerGui gui = holder.getGui();

            ItemStack clicked = event.getCurrentItem();
            if (clicked != null && clicked.getType() != Material.AIR) {
                gui.getParentGui().setActiveColor(ColorPalette.getMapColor(clicked.getType()));
                gui.getParentGui().open(); // Return to main GUI
            }
        }
    }
}
