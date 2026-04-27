package uk.co.webdent.vanillamaps.fabric.platform;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiConsumer;

public class FabricGuiBuilder {

    public static FabricGui create(int rows, Component title) {
        return new FabricGui(rows, title);
    }

    public static class FabricGui {
        private final int rows;
        private final int size;
        private final Component title;
        private final SimpleContainer container;
        private final Map<Integer, BiConsumer<ServerPlayer, ClickType>> clickHandlers = new HashMap<>();

        private static final Map<UUID, FabricGui> ACTIVE_GUIS = new HashMap<>();

        public FabricGui(int rows, Component title) {
            this.rows = rows;
            this.size = rows * 9;
            this.title = title;
            this.container = new SimpleContainer(size);
        }

        public void setItem(int slot, ItemStack stack) {
            if (slot >= 0 && slot < size) {
                container.setItem(slot, stack);
            }
        }

        public void setItem(int slot, ItemStack stack, BiConsumer<ServerPlayer, ClickType> handler) {
            setItem(slot, stack);
            if (handler != null) {
                clickHandlers.put(slot, handler);
            }
        }

        public ItemStack getItem(int slot) {
            if (slot >= 0 && slot < size) {
                return container.getItem(slot);
            }
            return ItemStack.EMPTY;
        }

        public void open(ServerPlayer player) {
            ACTIVE_GUIS.put(player.getUUID(), this);
            player.openMenu(new SimpleMenuProvider(
                    (syncId, playerInventory, p) -> createMenu(syncId, playerInventory),
                    title
            ));
        }

        private AbstractContainerMenu createMenu(int syncId, Inventory playerInventory) {
            MenuType<ChestMenu> menuType = switch (rows) {
                case 1 -> MenuType.GENERIC_9x1;
                case 2 -> MenuType.GENERIC_9x2;
                case 3 -> MenuType.GENERIC_9x3;
                case 4 -> MenuType.GENERIC_9x4;
                case 5 -> MenuType.GENERIC_9x5;
                default -> MenuType.GENERIC_9x6;
            };

            ChestMenu menu = new ChestMenu(menuType, syncId, playerInventory, container, rows) {
                @Override
                public void clicked(int slotId, int button, ClickType clickType, Player player) {
                    if (slotId >= 0 && slotId < size && player instanceof ServerPlayer sp) {
                        BiConsumer<ServerPlayer, ClickType> handler = clickHandlers.get(slotId);
                        if (handler != null) {
                            handler.accept(sp, clickType);
                        }
                        return;
                    }
                    if (slotId >= size) {
                        return;
                    }
                }

                @Override
                public boolean stillValid(Player player) {
                    return true;
                }
            };

            return menu;
        }

        public int getSize() {
            return size;
        }

        public int getRows() {
            return rows;
        }

        public Map<Integer, BiConsumer<ServerPlayer, ClickType>> getClickHandlers() {
            return clickHandlers;
        }

        public SimpleContainer getContainer() {
            return container;
        }

        public static FabricGui getActiveGui(UUID playerId) {
            return ACTIVE_GUIS.get(playerId);
        }

        public static void removeActiveGui(UUID playerId) {
            ACTIVE_GUIS.remove(playerId);
        }

        public void clearClickHandlers() {
            clickHandlers.clear();
        }
    }
}
