package uk.co.webdent.vanillamaps.api;

public interface IMapPlatform {
    void applyRenderer(int mapId, byte[] pixels);
    void reloadAllRenderers(java.io.File dataDir);
}
