package uk.co.webdent.vanillamaps.fabric.platform;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class FabricItemUtils {

    private FabricItemUtils() {}

    public static ItemStack woolNameToItem(String woolName) {
        return new ItemStack(switch (woolName) {
            case "black_wool"      -> Items.BLACK_WOOL;
            case "red_wool"        -> Items.RED_WOOL;
            case "orange_wool"     -> Items.ORANGE_WOOL;
            case "yellow_wool"     -> Items.YELLOW_WOOL;
            case "lime_wool"       -> Items.LIME_WOOL;
            case "green_wool"      -> Items.GREEN_WOOL;
            case "cyan_wool"       -> Items.CYAN_WOOL;
            case "light_blue_wool" -> Items.LIGHT_BLUE_WOOL;
            case "blue_wool"       -> Items.BLUE_WOOL;
            case "purple_wool"     -> Items.PURPLE_WOOL;
            case "magenta_wool"    -> Items.MAGENTA_WOOL;
            case "pink_wool"       -> Items.PINK_WOOL;
            case "brown_wool"      -> Items.BROWN_WOOL;
            case "gray_wool"       -> Items.GRAY_WOOL;
            case "light_gray_wool" -> Items.LIGHT_GRAY_WOOL;
            default                -> Items.WHITE_WOOL;
        });
    }
}
