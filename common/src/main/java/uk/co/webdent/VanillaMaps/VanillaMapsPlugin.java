package uk.co.webdent.VanillaMaps;

import uk.co.webdent.VanillaMaps.custommaps.CustomMapsModule;
import uk.co.webdent.VanillaMaps.adapter.VanillaMapsAdapter;
import org.bukkit.plugin.java.JavaPlugin;

public class VanillaMapsPlugin extends JavaPlugin {

    private CustomMapsModule customMapsModule;
    private VanillaMapsAdapter adapter;

    @Override
    public void onEnable() {
        getLogger().info("Loading VanillaMaps...");

        // Load correct version adapter
        String version = getServer().getBukkitVersion();
        try {
            if (version.contains("1.21.1")) {
                Class<?> adapterClass = Class.forName("uk.co.webdent.VanillaMaps.paper1_21.Paper121Adapter");
                adapter = (VanillaMapsAdapter) adapterClass.getDeclaredConstructor().newInstance();
            } else if (version.contains("26.1")) {
                Class<?> adapterClass = Class.forName("uk.co.webdent.VanillaMaps.paper1_21.Paper121Adapter");
                adapter = (VanillaMapsAdapter) adapterClass.getDeclaredConstructor().newInstance();
            }

            if (adapter != null) {
                adapter.onEnable();
            } else {
                getLogger().warning(
                        "No adapter found for version " + version + ". Core will run on strict native API fallback.");
            }
        } catch (Exception e) {
            getLogger().severe("Failed to initialize server-specific adapter: " + e.getMessage());
        }

        // 3. Initialize CustomMaps logic
        try {
            customMapsModule = new CustomMapsModule(this);
            customMapsModule.init();
            getLogger().info("Successfully loaded Custom Maps module.");
        } catch (Exception e) {
            getLogger().severe("Failed to load Custom Maps module: " + e.getMessage());
            e.printStackTrace();
        }

        getLogger().info("VanillaMaps loaded!");

        int pluginId = 30813;
        Metrics metrics = new Metrics(this, pluginId);
    }

    @Override
    public void onDisable() {
        if (customMapsModule != null) {
            customMapsModule.shutdown();
        }
        getLogger().info("VanillaMaps disabled!");
    }
}
