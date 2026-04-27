package uk.co.webdent.vanillamaps.paper.platform;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import uk.co.webdent.vanillamaps.api.ICommandRegistrar;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventManager;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public class PaperCommandRegistrar implements ICommandRegistrar {

    private final JavaPlugin plugin;

    public PaperCommandRegistrar(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void register(String name, String description, String permission, CommandExecutor executor, CommandCompleter completer) {
        LifecycleEventManager<org.bukkit.plugin.Plugin> manager = plugin.getLifecycleManager();
        manager.registerEventHandler(LifecycleEvents.COMMANDS, event -> {
            Commands commands = event.registrar();
            
            LiteralArgumentBuilder<CommandSourceStack> cmd = Commands.literal(name)
                    .requires(source -> permission == null || source.getSender().hasPermission(permission))
                    .then(Commands.argument("args", StringArgumentType.greedyString())
                        .suggests((ctx, builder) -> {
                            CommandSourceStack source = ctx.getSource();
                            CommandSender sender = source.getSender();
                            String[] args = ctx.getInput().replaceFirst("^/" + name, "").trim().split("\\s+");
                            java.util.UUID uuid = sender instanceof Player p ? p.getUniqueId() : null;
                            List<String> suggestions = completer.complete(uuid, args);
                            for (String s : suggestions) {
                                if (s.toLowerCase().startsWith(args[args.length - 1].toLowerCase())) {
                                    builder.suggest(s);
                                }
                            }
                            return builder.buildFuture();
                        })
                        .executes(ctx -> {
                            execute(ctx.getSource(), executor, StringArgumentType.getString(ctx, "args"));
                            return 1;
                        })
                    )
                    .executes(ctx -> {
                        execute(ctx.getSource(), executor, "");
                        return 1;
                    });
            
            commands.register(cmd.build(), description);
        });
    }
    
    private void execute(CommandSourceStack source, CommandExecutor executor, String rawArgs) {
        CommandSender sender = source.getSender();
        java.util.UUID uuid = sender instanceof Player p ? p.getUniqueId() : null;
        String senderName = sender instanceof Player p ? p.getName() : "Console";
        boolean isOp = sender.isOp();
        String[] argsArray = rawArgs.isEmpty() ? new String[0] : rawArgs.split("\\s+");
        
        executor.execute(uuid, senderName, isOp, argsArray);
    }
}
