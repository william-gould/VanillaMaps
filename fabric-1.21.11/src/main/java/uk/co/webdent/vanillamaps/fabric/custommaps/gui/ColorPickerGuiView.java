package uk.co.webdent.vanillamaps.fabric.custommaps.gui;

import uk.co.webdent.vanillamaps.util.MapColorPalette;
import uk.co.webdent.vanillamaps.fabric.platform.FabricGuiBuilder;
import uk.co.webdent.vanillamaps.fabric.platform.FabricItemUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.registries.BuiltInRegistries;

public class ColorPickerGuiView {
    private final ServerPlayer player;
    private final DrawingGuiView parentGui;

    public ColorPickerGuiView(ServerPlayer player, DrawingGuiView parentGui) {
        this.player = player;
        this.parentGui = parentGui;
    }

    public void open() {
        int size = ((MapColorPalette.ALL_COLOR_NAMES.size() / 9) + 1);
        FabricGuiBuilder.FabricGui gui = FabricGuiBuilder.create(size, Component.literal("Select a Colour"));

        for (int i = 0; i < MapColorPalette.ALL_COLOR_NAMES.size(); i++) {
            String name = MapColorPalette.ALL_COLOR_NAMES.get(i);
            
            ItemStack displayItem = FabricItemUtils.woolNameToItem(name);
            displayItem.set(net.minecraft.core.component.DataComponents.CUSTOM_NAME,
                Component.literal(name.replace("_wool", "").replace("_", " "))
                         .withStyle(s -> s.withItalic(false)));
            gui.setItem(i, displayItem, (p, c) -> {
                parentGui.getSession().setActiveColor(MapColorPalette.getMapColor(name));
                parentGui.open(); // return to canvas
            });
        }
        gui.open(player);
    }
}
