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

            if (slot >= 0 && slot < 45) { // Canvas cells
                switch (gui.getActiveTool()) {
                    case PENCIL -> {
                        if (event.isLeftClick()) {
                            gui.setPixel(slot, gui.getActiveColor());
                        } else if (event.isRightClick()) {
                            byte pickedColor = gui.getPixelColorAt(slot);
                            gui.setActiveColor(pickedColor);
                        }
                    }
                    case FILL -> {
                        if (event.isLeftClick()) {
                            gui.fillPixel(slot, gui.getActiveColor());
                        } else if (event.isRightClick()) {
                            gui.fillPixel(slot, ColorPalette.getMapColor(Material.WHITE_WOOL));
                        }
                    }
                    case NOISE -> {
                        gui.applyBackground(
                                uk.co.webdent.VanillaMaps.custommaps.gui.generator.BackgroundGenerator.BackgroundType.NOISE);
                    }
                    case CHECKERBOARD -> {
                        gui.applyBackground(
                                uk.co.webdent.VanillaMaps.custommaps.gui.generator.BackgroundGenerator.BackgroundType.CHECKERBOARD);
                    }
                    case BRICKS -> {
                        gui.applyBackground(
                                uk.co.webdent.VanillaMaps.custommaps.gui.generator.BackgroundGenerator.BackgroundType.BRICKS);
                    }
                    case TILES -> {
                        gui.applyBackground(uk.co.webdent.VanillaMaps.custommaps.gui.generator.BackgroundGenerator.BackgroundType.TILES);
                    }
                    case LINE -> {
                        if (event.isRightClick()) {
                            gui.clearLineFirstPoint();
                        } else if (event.isLeftClick()) {
                            if (gui.getFirstPointX() == -1) {
                                gui.setLineFirstPoint(slot);
                            } else {
                                gui.drawLine(slot, gui.getActiveColor());
                                gui.clearLineFirstPoint();
                            }
                        }
                    }
                    case RECTANGLE -> {
                        if (event.isRightClick()) {
                            gui.clearLineFirstPoint();
                        } else if (event.isLeftClick()) {
                            if (gui.getFirstPointX() == -1) {
                                gui.setLineFirstPoint(slot);
                            } else {
                                gui.drawRect(slot, gui.getActiveColor());
                                gui.clearLineFirstPoint();
                            }
                        }
                    }
                }

            } else if (slot == 45) { // ⬆ Move Up
                gui.pan(0, event.isShiftClick() ? -5 : -1);

            } else if (slot == 46) { // ⬅ Move Left
                gui.pan(event.isShiftClick() ? -9 : -1, 0);

            } else if (slot == 47) { // ➡ Move Right
                gui.pan(event.isShiftClick() ? 9 : 1, 0);

            } else if (slot == 48) { // ⬇ Move Down
                gui.pan(0, event.isShiftClick() ? 5 : 1);

            } else if (slot == 49) { // 🧰 Tools button
                if (event.isLeftClick()) {
                    // Open the tools submenu
                    ToolsGui toolsGui = new ToolsGui(player, gui);
                    toolsGui.open();
                } else if (event.isRightClick()) {
                    // Cycle to the next tool without opening the submenu
                    gui.cycleNextTool();
                }

            } else if (slot == 50) { // ✏ Colour picker
                ColorPickerGui picker = new ColorPickerGui(player, gui);
                picker.open();

            } else if (slot == 51) { // ↩ Undo
                gui.undo();

            } else if (slot == 52) { // ✖ Cancel
                player.closeInventory();

            } else if (slot == 53) { // ✔ Save
                plugin.getMapDataStore().save(player, gui);
                player.closeInventory();
            }

        } else if (event.getInventory().getHolder() instanceof ColorPickerGuiHolder holder) {
            event.setCancelled(true);
            ColorPickerGui gui = holder.getGui();

            ItemStack clicked = event.getCurrentItem();
            if (clicked != null && clicked.getType() != Material.AIR) {
                gui.getParentGui().setActiveColor(ColorPalette.getMapColor(clicked.getType()));
                gui.getParentGui().open();
            }

        } else if (event.getInventory().getHolder() instanceof ToolsGuiHolder holder) {
            event.setCancelled(true);
            ToolsGui gui = holder.getGui();

            int slot = event.getRawSlot();
            ToolsGui.Tool[] tools = ToolsGui.Tool.values();

            // Each tool occupies one slot starting at 0
            if (slot >= 0 && slot < tools.length) {
                ToolsGui.Tool selected = tools[slot];
                gui.getParentGui().setActiveTool(selected);
                gui.getParentGui().open(); // Return to drawing canvas
            }
        }
    }
}
