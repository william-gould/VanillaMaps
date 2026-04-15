package uk.co.webdent.VanillaMaps.paper1_21;

import uk.co.webdent.VanillaMaps.adapter.VanillaMapsAdapter;
import java.util.logging.Logger;

public class Paper121Adapter implements VanillaMapsAdapter {

    private final Logger logger;

    public Paper121Adapter() {
        this.logger = Logger.getLogger("VanillaMaps-1.21");
    }

    @Override
    public String getAdapterVersion() {
        return "1.21.1";
    }

    @Override
    public void onEnable() {
        logger.info("Successfully bound Paper 1.21.1 Adapter logic.");
        // Initialize NMS or experimental logic unique to 1.21 here!
    }

    @Override
    public void onDisable() {
        logger.info("Unloaded Paper 1.21.1 Adapter.");
    }
}
