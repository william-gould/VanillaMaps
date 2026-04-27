package uk.co.webdent.vanillamaps.api;

import java.util.List;
import java.util.UUID;

public interface ICommandRegistrar {

    void register(
        String name,
        String description,
        String permission,
        CommandExecutor executor,
        CommandCompleter completer
    );

    @FunctionalInterface
    interface CommandExecutor {
        void execute(UUID senderUuid, String senderName, boolean isOp, String[] args);
    }

    @FunctionalInterface
    interface CommandCompleter {
        List<String> complete(UUID senderUuid, String[] args);
    }
}
