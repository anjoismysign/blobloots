package io.github.anjoismysign.blobloots.command;

import io.github.anjoismysign.bloblib.api.BlobLibMessageAPI;
import io.github.anjoismysign.bloblib.message.BlobMessage;
import io.github.anjoismysign.blobloots.BlobLoots;
import io.github.anjoismysign.blobloots.asset.ChestLoot;
import io.github.anjoismysign.blobloots.util.WandUtil;
import io.github.anjoismysign.skeramidcommands.command.Command;
import io.github.anjoismysign.skeramidcommands.command.CommandTarget;
import io.github.anjoismysign.skeramidcommands.commandtarget.CommandTargetBuilder;
import io.github.anjoismysign.skeramidcommands.server.PermissionMessenger;
import io.github.anjoismysign.skeramidcommands.server.bukkit.BukkitAdapter;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public enum BlobLootsCommand {
    INSTANCE;

    private static final String COMMAND_NAME = "blobloots";
    private static final String COMMAND_PERMISSION = "blobloots";
    private static final String COMMAND_DESCRIPTION = "Base command for blobloots plugin";

    private static final Command COMMAND = BukkitAdapter.getInstance().createCommand(COMMAND_NAME, COMMAND_PERMISSION, COMMAND_DESCRIPTION);

    private static final BlobLibMessageAPI MESSAGE_API = BlobLibMessageAPI.getInstance();

    public void load(){
        CommandTarget<ChestLoot> chestLootCommandTarget = CommandTargetBuilder.fromMap(()-> BlobLoots.getInstance().getManagerDirector().getChestLootManager().getChestLoots());

        Command chestLootCommand = COMMAND.child("chestloot");
        Command chestLootGetCommand = chestLootCommand.child("get");
        chestLootGetCommand.setParameters(chestLootCommandTarget);
        chestLootGetCommand.onExecute(((permissionMessenger, args) -> {
            if (args.length < 1){
                return;
            }
            @Nullable Player player = player(permissionMessenger);
            if (player == null){
                return;
            }
            String playerLocale = player.getLocale();
            String chestLootIdentifier = args[0];
            @Nullable ChestLoot chestLoot = chestLootCommandTarget.parse(chestLootIdentifier);
            if (chestLoot == null){
                BlobMessage.by("ChestLoot.Not-Found")
                        .localize(playerLocale)
                        .modder()
                        .replace("%chestLoot%", chestLootIdentifier)
                        .get()
                        .handle(player);
                return;
            }
            WandUtil.INSTANCE.giveChestLootWand(player, chestLoot);
        }));
    }

    @Nullable
    private Player player(@NotNull PermissionMessenger permissionMessenger) {
        CommandSender sender = BukkitAdapter.getInstance().of(permissionMessenger);
        if (!(sender instanceof Player player)) {
            BlobLibMessageAPI.getInstance()
                    .getMessage("System.Console-Not-Allowed-Command", sender)
                    .toCommandSender(sender);
            return null;
        }
        return player;
    }
}
