package uk.co.webdent.vanillamaps.paper.platform;

import uk.co.webdent.vanillamaps.api.IGuiPlatform;
import uk.co.webdent.vanillamaps.feature.custommaps.DrawingSession;
import uk.co.webdent.vanillamaps.paper.custommaps.gui.DrawingGuiView;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

public class PaperGuiPlatform implements IGuiPlatform {

    @Override
    public void openDrawingGui(UUID playerUuid, DrawingSession session) {
        Player player = Bukkit.getPlayer(playerUuid);
        if (player != null) {
            DrawingGuiView view = new DrawingGuiView(player, session);
            view.open();
        }
    }

    @Override
    public void openColorPickerGui(UUID playerUuid, DrawingSession session) {
        Player player = Bukkit.getPlayer(playerUuid);
        if (player != null) {
            DrawingGuiView view = new DrawingGuiView(player, session);
            view.getColorPickerGui().open();
        }
    }

    @Override
    public void openToolsGui(UUID playerUuid, DrawingSession session) {
        Player player = Bukkit.getPlayer(playerUuid);
        if (player != null) {
            DrawingGuiView view = new DrawingGuiView(player, session);
            view.getToolsGui().open();
        }
    }
}
