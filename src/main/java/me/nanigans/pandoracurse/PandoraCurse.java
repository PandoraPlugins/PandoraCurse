package me.nanigans.pandoracurse;

import me.nanigans.pandoracurse.Commands.Swear;
import me.nanigans.pandoracurse.Events.SwearListener;
import org.bukkit.plugin.java.JavaPlugin;

public final class PandoraCurse extends JavaPlugin {

    @Override
    public void onEnable() {
        // Plugin startup logic

        getCommand("addswear").setExecutor(new Swear());
        getServer().getPluginManager().registerEvents(new SwearListener(), this);


    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
