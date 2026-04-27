package uk.co.webdent.vanillamaps.fabric.platform;

import uk.co.webdent.vanillamaps.api.IGuiPlatform;
import uk.co.webdent.vanillamaps.feature.custommaps.DrawingSession;
import uk.co.webdent.vanillamaps.fabric.custommaps.gui.DrawingGuiView;
import uk.co.webdent.vanillamaps.fabric.custommaps.gui.ColorPickerGuiView;
import uk.co.webdent.vanillamaps.fabric.custommaps.gui.ToolsGuiView;
import net.minecraft.server.level.ServerPlayer;
import uk.co.webdent.vanillamaps.fabric.VanillaMapsMod;

import java.util.UUID;

public class FabricGuiPlatform implements IGuiPlatform {
    @Override
    public void openDrawingGui(UUID playerUuid, DrawingSession session) {
        ServerPlayer player = VanillaMapsMod.getInstance().getServer().getPlayerList().getPlayer(playerUuid);
        if (player != null) {
            new DrawingGuiView(player, session).open();
        }
    }

    @Override
    public void openColorPickerGui(UUID playerUuid, DrawingSession session) {
        ServerPlayer player = VanillaMapsMod.getInstance().getServer().getPlayerList().getPlayer(playerUuid);
        if (player != null) {
            DrawingGuiView gui = new DrawingGuiView(player, session);
            new ColorPickerGuiView(player, gui).open();
        }
    }

    @Override
    public void openToolsGui(UUID playerUuid, DrawingSession session) {
        ServerPlayer player = VanillaMapsMod.getInstance().getServer().getPlayerList().getPlayer(playerUuid);
        if (player != null) {
            DrawingGuiView gui = new DrawingGuiView(player, session);
            new ToolsGuiView(player, gui).open();
        }
    }
}
