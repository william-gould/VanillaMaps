package uk.co.webdent.vanillamaps.fabric.mixin;

import net.minecraft.world.level.saveddata.maps.MapDecoration;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.Map;

@Mixin(MapItemSavedData.class)
public interface MapItemSavedDataAccessor {
    @Invoker("setColorsDirty")
    void invokeSetColorsDirty(int x, int z);

    @Accessor("locked")
    void setLocked(boolean locked);

    @Accessor("trackingPosition")
    void setTrackingPosition(boolean trackingPosition);

    @Accessor("decorations")
    Map<String, MapDecoration> getDecorations();
}
