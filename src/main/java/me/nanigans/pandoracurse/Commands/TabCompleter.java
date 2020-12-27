package me.nanigans.pandoracurse.Commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class TabCompleter implements org.bukkit.command.TabCompleter {
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {

        if(command.getName().equalsIgnoreCase("swear")){

            switch (args.length) {
                case 1:
                    return Arrays.asList("add", "remove");
                case 2:
                    return Collections.singletonList("word");
            }

        }

        return null;
    }
}
