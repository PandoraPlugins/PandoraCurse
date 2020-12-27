package me.nanigans.pandoracurse.Commands;

import me.nanigans.pandoracurse.Inventory.BlackListInventory;
import me.nanigans.pandoracurse.Inventory.ShowAllInventory;
import me.nanigans.pandoracurse.PandoraCurse;
import me.nanigans.pandoracurse.SwearWords.BlackListWords;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Swear implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if(command.getName().equals("swear")) {
            if (sender instanceof Player) {
                Player player = ((Player) sender);

                if (args.length == 0) {
                    if(PandoraCurse.hasPermsTo(player, "Swear.ViewAll")) {
                        new ShowAllInventory(player);
                    } else{
                        player.sendMessage(ChatColor.RED+"Invalid permissions to see all swears.");
                    }
                } else {
                    if (args.length > 1) {
                        if (args[0].equalsIgnoreCase("add")) {

                            new BlackListInventory(player, args[1]);

                        } else if (args[0].equalsIgnoreCase("remove")) {

                            String removeWord = args[1];
                            BlackListWords.removeWord(removeWord);
                            sender.sendMessage(ChatColor.GREEN + "Removed word: " + removeWord);

                        }

                    } else {
                        sender.sendMessage(ChatColor.RED + "Please specify the word you want to add/remove");
                    }
                }

            } else {
                sender.sendMessage(ChatColor.RED + "Only players may use this command");
            }
            return true;

        }
        return false;
    }
}
