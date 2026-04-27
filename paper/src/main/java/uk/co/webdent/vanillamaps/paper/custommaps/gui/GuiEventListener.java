package uk.co.webdent.vanillamaps.paper.custommaps.gui;

import uk.co.webdent.vanillamaps.feature.custommaps.DrawingSession;
import uk.co.webdent.vanillamaps.feature.custommaps.MapCommandLogic;
import uk.co.webdent.vanillamaps.util.MapColorPalette;
import uk.co.webdent.vanillamaps.util.BackgroundGenerator;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.MapMeta;

public class GuiEventListener implements Listener {

    private final MapCommandLogic mapCommandLogic;

    public GuiEventListener(MapCommandLogic mapCommandLogic) {
        this.mapCommandLogic = mapCommandLogic;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player))
            return;

        if (event.getInventory().getHolder() instanceof DrawingGuiHolder holder) {
            event.setCancelled(true);
            DrawingGuiView gui = holder.getGui();
            DrawingSession session = gui.getSession();
            int slot = event.getRawSlot();

            if (slot >= 0 && slot < 45) { // Canvas cells
                switch (session.getActiveTool()) {
                    case PENCIL -> {
                        if (event.isLeftClick()) {
                            session.setPixelAtSlot(slot, session.getActiveColor());
                        } else if (event.isRightClick()) {
                            byte pickedColor = session.getPixelColorAtSlot(slot);
                            session.setActiveColor(pickedColor);
                        }
                    }
                    case FILL -> {
                        if (event.isLeftClick()) {
                            session.fillPixelAtSlot(slot, session.getActiveColor());
                        } else if (event.isRightClick()) {
                            session.fillPixelAtSlot(slot, MapColorPalette.getMapColor("white_wool"));
                        }
                    }
                    case NOISE -> {
                        session.applyBackground(BackgroundGenerator.BackgroundType.NOISE);
                    }
                    case CHECKERBOARD -> {
                        session.applyBackground(BackgroundGenerator.BackgroundType.CHECKERBOARD);
                    }
                    case BRICKS -> {
                        session.applyBackground(BackgroundGenerator.BackgroundType.BRICKS);
                    }
                    case TILES -> {
                        session.applyBackground(BackgroundGenerator.BackgroundType.TILES);
                    }
                    case LINE -> {
                        if (event.isRightClick()) {
                            session.clearLineFirstPoint();
                        } else if (event.isLeftClick()) {
                            if (session.getFirstPointX() == -1) {
                                session.setLineFirstPoint(slot);
                            } else {
                                session.drawLineToSlot(slot, session.getActiveColor());
                                session.clearLineFirstPoint();
                            }
                        }
                    }
                    case RECTANGLE -> {
                        if (event.isRightClick()) {
                            session.clearLineFirstPoint();
                        } else if (event.isLeftClick()) {
                            if (session.getFirstPointX() == -1) {
                                session.setLineFirstPoint(slot);
                            } else {
                                session.drawRectToSlot(slot, session.getActiveColor());
                                session.clearLineFirstPoint();
                            }
                        }
                    }
                }
                gui.buildCanvas();

            } else if (slot == 45) { // ⬆ Move Up
                session.pan(0, event.isShiftClick() ? -5 : -1);
                gui.buildCanvas();
            } else if (slot == 46) { // ⬅ Move Left
                session.pan(event.isShiftClick() ? -9 : -1, 0);
                gui.buildCanvas();
            } else if (slot == 47) { // ➡ Move Right
                session.pan(event.isShiftClick() ? 9 : 1, 0);
                gui.buildCanvas();
            } else if (slot == 48) { // ⬇ Move Down
                session.pan(0, event.isShiftClick() ? 5 : 1);
                gui.buildCanvas();

            } else if (slot == 49) { // 🧰 Tools button
                if (event.isLeftClick()) {
                    gui.getToolsGui().open();
                } else if (event.isRightClick()) {
                    session.cycleNextTool();
                    gui.updateToolsButton();
                }

            } else if (slot == 50) { // ✏ Colour picker
                gui.getColorPickerGui().open();

            } else if (slot == 51) { // ↩ Undo
                session.undo();
                gui.buildCanvas();

            } else if (slot == 52) { // ✖ Cancel
                player.closeInventory();

            } else if (slot == 53) { // ✔ Save
                ItemStack mainHand = player.getInventory().getItemInMainHand();
                if (mainHand.getType() == Material.FILLED_MAP && mainHand.getItemMeta() instanceof MapMeta mapMeta) {
                    mapCommandLogic.onSave(player.getUniqueId(), mapMeta.hasMapId() ? mapMeta.getMapId() : -1, session);
                } else {
                    player.sendMessage("§cYou must hold a filled map in your main hand to save!");
                }
                player.closeInventory();
            }

        } else if (event.getInventory().getHolder() instanceof ColorPickerGuiHolder holder) {
            event.setCancelled(true);
            ColorPickerGuiView gui = holder.getGui();

            ItemStack clicked = event.getCurrentItem();
            if (clicked != null && clicked.getType() != Material.AIR) {
                gui.getParentGui().getSession().setActiveColor(MapColorPalette.getMapColor(clicked.getType().name().toLowerCase()));
                gui.getParentGui().updateActiveColorDisplay();
                gui.getParentGui().open();
            }

        } else if (event.getInventory().getHolder() instanceof ToolsGuiHolder holder) {
            event.setCancelled(true);
            ToolsGuiView gui = holder.getGui();

            int slot = event.getRawSlot();
            DrawingSession.Tool[] tools = DrawingSession.Tool.values();

            if (slot >= 0 && slot < tools.length) {
                DrawingSession.Tool selected = tools[slot];
                gui.getParentGui().getSession().setActiveTool(selected);
                gui.getParentGui().updateToolsButton();
                gui.getParentGui().open();
            }
        }
    }
}
