package me.nanigans.pandoracurse.Events;

import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.FPlayers;
import com.massivecraft.factions.struct.ChatMode;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class SwearListener implements Listener {

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event){
        final FPlayer fPlayer = FPlayers.getInstance().getByPlayer(event.getPlayer());
        if(fPlayer.getChatMode() == ChatMode.PUBLIC){

            final String message = event.getMessage();


        }

    }

}
