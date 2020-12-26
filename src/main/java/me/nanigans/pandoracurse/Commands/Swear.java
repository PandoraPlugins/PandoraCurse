package me.nanigans.pandoracurse.Commands;

import me.nanigans.pandoracurse.Inventory.BlackListInventory;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Swear implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if(sender instanceof Player){

            if(args.length > 0){

                Player player = ((Player) sender);
                new BlackListInventory(player, args[0]);

            }else{
                sender.sendMessage(ChatColor.RED+"Please specify the word to blacklist");
            }

        }else{
            sender.sendMessage(ChatColor.RED+"Only players may use this command");
            return true;
        }

        return false;
    }
}
