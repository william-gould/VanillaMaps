package uk.co.webdent.vanillamaps.fabric.config;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

@Config(name = "VanillaMaps")
public class VanillaMapsConfig implements ConfigData {

    @ConfigEntry.Category("general")
    public String defaultBackground = "WHITE";

    @ConfigEntry.Category("messages")
    public String mustBePlayer = "Only players can use this command.";

    @ConfigEntry.Category("messages")
    public String notHoldingMap = "You must be holding a map.";

    @ConfigEntry.Category("messages")
    public String mustFillMap = "You must right-click to fill the map first before saving.";

    @ConfigEntry.Category("messages")
    public String saveSuccess = "Map saved successfully!";
}
