package uk.co.webdent.vanillamaps.fabric.mixin;

import uk.co.webdent.vanillamaps.fabric.platform.FabricMapPlatform;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

@Mixin(ServerLevel.class)
public class MapDataLazyMixin {

    @Inject(method = "getMapData", at = @At("RETURN"), cancellable = true)
    private void VanillaMaps$onGetMapData(MapId mapId, CallbackInfoReturnable<MapItemSavedData> cir) {
        MapItemSavedData data = cir.getReturnValue();
        int id = mapId.id();

        if (data == null) {
            if (FabricMapPlatform.PENDING_FILES.containsKey(id) || FabricMapPlatform.PENDING_PIXELS.containsKey(id)) {
                data = MapItemSavedData.createFresh(0, 0, (byte) 0, false, false, 
                        ((ServerLevel)(Object)this).dimension());
                
                ((ServerLevel)(Object)this).setMapData(mapId, data);
                cir.setReturnValue(data);
            } else {
                return;
            }
        }

        byte[] pixels = FabricMapPlatform.PENDING_PIXELS.remove(id);
        if (pixels != null) {
            System.arraycopy(pixels, 0, data.colors, 0,
                    Math.min(pixels.length, data.colors.length));
            applyDataSettings(data);
            FabricMapPlatform.PENDING_FILES.remove(id); 
            return;
        }

        File file = FabricMapPlatform.PENDING_FILES.remove(id);
        if (file != null && file.exists()) {
            try {
                byte[] filePixels = Files.readAllBytes(file.toPath());
                if (filePixels.length == 128 * 128) {
                    System.arraycopy(filePixels, 0, data.colors, 0, filePixels.length);
                    applyDataSettings(data);
                }
            } catch (IOException e) {
                org.slf4j.LoggerFactory.getLogger("VanillaMaps")
                        .error("Failed to lazy-load map {}: {}", id, e.getMessage());
            }
        }
    }

    private void applyDataSettings(MapItemSavedData data) {
        MapItemSavedDataAccessor accessor = (MapItemSavedDataAccessor) data;

        accessor.setLocked(true);

        accessor.setTrackingPosition(false);
        
        accessor.getDecorations().clear();
        
        accessor.invokeSetColorsDirty(0, 0);
        accessor.invokeSetColorsDirty(127, 127);

        data.setDirty();
    }
}
