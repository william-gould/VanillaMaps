package uk.co.webdent.vanillamaps.api;

import uk.co.webdent.vanillamaps.feature.custommaps.DrawingSession;
import java.util.UUID;

public interface IGuiPlatform {
    void openDrawingGui(UUID playerUuid, DrawingSession session);
    void openColorPickerGui(UUID playerUuid, DrawingSession session);
    void openToolsGui(UUID playerUuid, DrawingSession session);
}
