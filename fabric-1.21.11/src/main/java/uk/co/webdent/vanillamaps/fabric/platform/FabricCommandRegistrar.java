package uk.co.webdent.vanillamaps.fabric.platform;

import uk.co.webdent.vanillamaps.api.ICommandRegistrar;
import uk.co.webdent.vanillamaps.feature.custommaps.MapCommandLogic;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.level.saveddata.maps.MapId;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class FabricCommandRegistrar implements ICommandRegistrar {

    private final List<Consumer<CommandDispatcher<CommandSourceStack>>> pending = new ArrayList<>();

    private static boolean isOp(CommandSourceStack source) {
        try {
            for (java.lang.reflect.Method m : CommandSourceStack.class.getMethods()) {
                if (m.getReturnType() == boolean.class && m.getParameterCount() == 1
                        && m.getParameterTypes()[0] == int.class) {
                    return (Boolean) m.invoke(source, 4);
                }
            }
        } catch (Exception e) {
        }
        return false;
    }

    public void registerAll(MapCommandLogic mapLogic) {
        pending.add(dispatcher -> {
            LiteralArgumentBuilder<CommandSourceStack> builder = Commands.literal("map")
                    .requires(source -> true)
                    .then(Commands.argument("args", StringArgumentType.greedyString())
                            .suggests((ctx, suggestionBuilder) -> {
                                if (!(ctx.getSource().getEntity() instanceof ServerPlayer player))
                                    return suggestionBuilder.buildFuture();
                                String argStr = ctx.getInput().replaceFirst("^/map", "").trim();
                                String[] args = argStr.isEmpty() ? new String[0] : argStr.split("\\s+");
                                List<String> suggestions = mapLogic.suggest(player.getUUID(), args);
                                String lastArg = args.length > 0 ? args[args.length - 1].toLowerCase() : "";
                                for (String s : suggestions) {
                                    if (s.toLowerCase().startsWith(lastArg)) {
                                        suggestionBuilder.suggest(s);
                                    }
                                }
                                return suggestionBuilder.buildFuture();
                            })
                            .executes(context -> executeMapCommand(context, mapLogic)));
            dispatcher.register(builder);
        });

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            for (Consumer<CommandDispatcher<CommandSourceStack>> c : pending) {
                c.accept(dispatcher);
            }
        });
    }

    private int executeMapCommand(CommandContext<CommandSourceStack> context, MapCommandLogic mapLogic) {
        if (!(context.getSource().getEntity() instanceof ServerPlayer player))
            return 1;

        String argStr = StringArgumentType.getString(context, "args");
        String[] args = argStr.split("\\s+");
        if (args.length == 0)
            return 1;

        ItemStack item = player.getMainHandItem();
        int mapId = -1;
        if (item.is(Items.FILLED_MAP)) {
            MapId id = item.get(DataComponents.MAP_ID);
            if (id != null) {
                mapId = id.id();
            }
        }

        String sub = args[0].toLowerCase();
        java.util.UUID uuid = player.getUUID();

        switch (sub) {
            case "copy" -> mapLogic.onCopy(uuid, mapId);
            case "paste" -> mapLogic.onPaste(uuid, mapId);
            case "create" -> mapLogic.onCreate(uuid, mapId, args.length > 1 ? Integer.parseInt(args[1]) : 1);
            case "edit" -> mapLogic.onEdit(uuid, mapId, args.length > 1 ? Integer.parseInt(args[1]) : 1);
            case "publish" -> mapLogic.onPublish(uuid, mapId);
            case "help" -> mapLogic.onHelp(uuid);
            case "write" -> {
                if (args.length < 5) {
                    player.sendSystemMessage(net.minecraft.network.chat.Component
                            .literal("§cUsage: /map write <line> <color> <alignment> <text...>"));
                    return 1;
                }
                try {
                    int line = Integer.parseInt(args[1]);
                    byte col = uk.co.webdent.vanillamaps.util.MapColorPalette.getMapColor(args[2] + "_wool");
                    String align = args[3].toLowerCase();
                    StringBuilder t = new StringBuilder();
                    for (int i = 4; i < args.length; i++)
                        t.append(args[i]).append(" ");
                    mapLogic.onWrite(uuid, mapId, t.toString().trim(), line, col, align);
                } catch (NumberFormatException e) {
                    player.sendSystemMessage(
                            net.minecraft.network.chat.Component.literal("§cInvalid line number. Must be an integer."));
                } catch (Exception e) {
                    player.sendSystemMessage(
                            net.minecraft.network.chat.Component.literal("§cAn error occurred: " + e.getMessage()));
                }
            }
        }
        return 1;
    }

    @Override
    public void register(String name, String description, String permission, CommandExecutor executor,
            CommandCompleter completer) {
        pending.add(dispatcher -> {
            LiteralArgumentBuilder<CommandSourceStack> builder = Commands.literal(name);

            if ("adm".equalsIgnoreCase(name)) {
                builder.requires(FabricCommandRegistrar::isOp);
            } else {
                builder.requires(source -> true);
            }

            builder.executes(context -> {
                ServerPlayer player = null;
                if (context.getSource().getEntity() instanceof ServerPlayer sp)
                    player = sp;
                boolean opStat = isOp(context.getSource());
                executor.execute(player != null ? player.getUUID() : null,
                        player != null ? player.getName().getString() : "Console", opStat, new String[0]);
                return 1;
            });

            builder.then(Commands.argument("args", StringArgumentType.greedyString())
                    .suggests((ctx, suggestionBuilder) -> {
                        if (completer != null) {
                            ServerPlayer player = null;
                            if (ctx.getSource().getEntity() instanceof ServerPlayer sp)
                                player = sp;
                            String argStr = ctx.getInput().replaceFirst("^/" + name, "").trim();
                            String[] args = argStr.isEmpty() ? new String[0] : argStr.split("\\s+");
                            List<String> suggestions = completer.complete(player != null ? player.getUUID() : null,
                                    args);
                            String lastArg = args.length > 0 ? args[args.length - 1].toLowerCase() : "";
                            for (String s : suggestions) {
                                if (s.toLowerCase().startsWith(lastArg)) {
                                    suggestionBuilder.suggest(s);
                                }
                            }
                        }
                        return suggestionBuilder.buildFuture();
                    })
                    .executes(context -> {
                        String argStr = StringArgumentType.getString(context, "args");
                        String[] args = argStr.split("\\s+");
                        ServerPlayer player = null;
                        if (context.getSource().getEntity() instanceof ServerPlayer sp)
                            player = sp;
                        boolean opStat = isOp(context.getSource());
                        executor.execute(player != null ? player.getUUID() : null,
                                player != null ? player.getName().getString() : "Console", opStat, args);
                        return 1;
                    }));

            dispatcher.register(builder);
        });
    }
}
