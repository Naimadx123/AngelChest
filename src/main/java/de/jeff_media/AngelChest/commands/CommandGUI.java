package de.jeff_media.AngelChest.commands;

import de.jeff_media.AngelChest.Main;
import de.jeff_media.AngelChest.enums.Features;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class CommandGUI implements CommandExecutor {

    final Main main;

    public CommandGUI() {
        this.main = Main.getInstance();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String[] args) {

        if(!main.premium(Features.GUI)) {
            sender.sendMessage(main.messages.MSG_PREMIUMONLY);
            return true;
        }

        if(!sender.hasPermission("angelchest.use")) {
            sender.sendMessage(main.getCommand("aclist").getPermissionMessage());
            return true;
        }

        if(!(sender instanceof Player)) {
            sender.sendMessage(main.messages.MSG_PLAYERSONLY);
            return true;
        }

        Player player = (Player) sender;

        main.guiManager.showMainGUI(player);

        return true;
    }
}