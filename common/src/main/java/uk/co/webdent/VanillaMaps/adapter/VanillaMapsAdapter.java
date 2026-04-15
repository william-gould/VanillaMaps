package uk.co.webdent.VanillaMaps.adapter;

public interface VanillaMapsAdapter {

    /**
     * @return The specific server version this adapter is targeting.
     */
    String getAdapterVersion();

    /**
     * Initializes any version-specific NMS hacks or alternative routines.
     */
    void onEnable();

    /**
     * Cleans up version-specific logic.
     */
    void onDisable();
}
