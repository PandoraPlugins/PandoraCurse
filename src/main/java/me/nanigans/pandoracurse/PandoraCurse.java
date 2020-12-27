package me.nanigans.pandoracurse;

import me.TechsCode.UltraPermissions.UltraPermissions;
import me.nanigans.pandoracurse.Commands.Swear;
import me.nanigans.pandoracurse.Events.SwearListener;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public final class PandoraCurse extends JavaPlugin {

    @Override
    public void onEnable() {
        // Plugin startup logic

        getCommand("swear").setExecutor(new Swear());
        getServer().getPluginManager().registerEvents(new SwearListener(), this);


    }

    public static boolean hasPermsTo(Player player, String permission){

        return UltraPermissions.getAPI().getUsers().uuid(player.getUniqueId()).getGroups().stream().flatMap(j -> j.getAdditionalPermissions().stream()).anyMatch(j -> j.getName().equals(permission))
                || UltraPermissions.getAPI().getUsers().uuid(player.getUniqueId()).getGroups().stream().flatMap(j -> j.getPermissions().stream()).anyMatch(j -> j.getName().equals(permission))
                || UltraPermissions.getAPI().getUsers().uuid(player.getUniqueId()).getAllPermissions().stream().anyMatch(j -> j.getName().equals(permission));

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
