package uk.co.webdent.vanillamaps.fabric.custommaps.gui;

import uk.co.webdent.vanillamaps.feature.custommaps.DrawingSession;
import uk.co.webdent.vanillamaps.fabric.platform.FabricGuiBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.core.component.DataComponents;

public class ToolsGuiView {

    private final ServerPlayer player;
    private final DrawingGuiView parentGui;

    public ToolsGuiView(ServerPlayer player, DrawingGuiView parentGui) {
        this.player = player;
        this.parentGui = parentGui;
    }

    public void open() {
        FabricGuiBuilder.FabricGui gui = FabricGuiBuilder.create(1, Component.literal("Select a Tool"));

        DrawingSession.Tool[] tools = DrawingSession.Tool.values();
        DrawingSession.Tool activeTool = parentGui.getSession().getActiveTool();

        for (int i = 0; i < tools.length; i++) {
            DrawingSession.Tool tool = tools[i];
            boolean isActive = tool == activeTool;
            
            ItemLike mat = switch (tool) {
                case PENCIL -> Items.FEATHER;
                case FILL -> Items.WATER_BUCKET;
                case LINE -> Items.STRING;
                case RECTANGLE -> Items.BLACK_STAINED_GLASS_PANE;
                case NOISE -> Items.GRAVEL;
                case CHECKERBOARD -> Items.DAYLIGHT_DETECTOR;
                case BRICKS -> Items.BRICKS;
                case TILES -> Items.QUARTZ_PILLAR;
            };

            ItemStack item = new ItemStack(mat);
            Component name = isActive
                    ? Component.literal("★ " + tool.name() + " (Active)").withStyle(s -> s.withColor(0xFFAA00).withItalic(false))
                    : Component.literal(tool.name()).withStyle(s -> s.withColor(0xFFFF55).withItalic(false));
            
            item.set(DataComponents.CUSTOM_NAME, name);

            gui.setItem(i, item, (p, c) -> {
                parentGui.getSession().setActiveTool(tool);
                parentGui.open(); // return to drawing canvas
            });
        }
        gui.open(player);
    }
}
