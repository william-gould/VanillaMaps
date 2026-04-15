package uk.co.webdent.VanillaMaps.paper26_1;

import uk.co.webdent.VanillaMaps.adapter.VanillaMapsAdapter;
import java.util.logging.Logger;

public class Paper261Adapter implements VanillaMapsAdapter {

    private final Logger logger;

    public Paper261Adapter() {
        this.logger = Logger.getLogger("VanillaMaps-26.1.2");
    }

    @Override
    public String getAdapterVersion() {
        return "26.1.2";
    }

    @Override
    public void onEnable() {
        logger.info("Successfully bound Paper 26.1.2 Adapter logic.");
        // Initialize NMS or experimental logic unique to 26.1.2 here!
    }

    @Override
    public void onDisable() {
        logger.info("Unloaded Paper 26.1.2 Adapter.");
    }
}
