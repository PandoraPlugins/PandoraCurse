package me.nanigans.pandoracurse.Events;

import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.FPlayers;
import com.massivecraft.factions.struct.ChatMode;
import me.nanigans.pandoracurse.PandoraCurse;
import me.nanigans.pandoracurse.SwearWords.BlackListWords;
import me.nanigans.pandoracurse.Utils.JsonUtils;
import me.xdrop.fuzzywuzzy.FuzzySearch;
import me.xdrop.fuzzywuzzy.model.ExtractedResult;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SwearListener implements Listener {
    private final static PandoraCurse plugin = PandoraCurse.getPlugin(PandoraCurse.class);

    /**
     * When player talks, we check if their message contains a bad word
     * @param event player chat event
     */
    @EventHandler
    public void onChat(AsyncPlayerChatEvent event){
        final Player player = event.getPlayer();
        final FPlayer fPlayer = FPlayers.getInstance().getByPlayer(player);
        if(fPlayer.getChatMode() == ChatMode.PUBLIC && !PandoraCurse.hasPermsTo(player, "Swear.Bypass")){

            final String message = event.getMessage();
            String curseMsg = message.replaceAll("[^a-zA-Z0-9 ]", "").replaceAll("\\s+", "").toLowerCase();
            String[] words = message.toLowerCase().split(" ");
            final Map<String, Object> allWords = BlackListWords.getAllWords();

            for (Map.Entry<String, Object> word : allWords.entrySet()) {
                final String wordMsg = word.getKey();
                final Map<String, Object> value = (Map<String, Object>) word.getValue();
                boolean alertStaff = Boolean.parseBoolean(value.get("alertStaff").toString());

                if(Boolean.parseBoolean(value.get("sensitive").toString())){//checking for sensitive message
                    if(curseMsg.toLowerCase().contains(wordMsg.toLowerCase())){
                        event.setCancelled(true);
                        fPlayer.sendMessage(JsonUtils.getData("message.messageDeleted"));
                        logWord(fPlayer, wordMsg);
                        if(alertStaff)
                            alertStaff(fPlayer, wordMsg, message);
                        return;
                    }
                }
                if(Boolean.parseBoolean(value.get("useFuzzySet").toString())){//checking with fuzzy set

                    final List<ExtractedResult> results = FuzzySearch.extractAll(wordMsg, Arrays.asList(words))
                            .stream().filter(i -> i.getScore() >= Integer.parseInt(value.get("fuzzyTolerance").toString())).collect(Collectors.toList());
                    if(results.size() > 0){
                        event.setCancelled(true);
                        logWord(fPlayer, wordMsg);
                        fPlayer.sendMessage(JsonUtils.getData("message.messageDeleted"));
                        if(alertStaff)
                            alertStaff(fPlayer, wordMsg, message);
                        return;
                    }

                }

                if(Arrays.stream(words).anyMatch(i -> i.toLowerCase().contains(wordMsg.toLowerCase()))){//checking for normal usage or swear
                    event.setCancelled(true);
                    logWord(fPlayer, wordMsg);
                    fPlayer.sendMessage(JsonUtils.getData("message.messageDeleted"));
                    if(alertStaff)
                        alertStaff(fPlayer, wordMsg, message);
                    return;
                }

            }

        }

    }

    /**
     * Alert staff member that a player has sworn
     * @param player Player to report
     * @param word the word they said
     */
    public void alertStaff(FPlayer player, String word, String message){
        final String str = player.getName() + ": " + message;
        final String replace = JsonUtils.getData("message.staffAlertMsg").replace("{playerName}", player.getName())
                .replace("{word}", word).replace("{worldName}", player.getPlayer().getWorld().getName());
        TextComponent msg = new TextComponent(replace);
        msg.setHoverEvent( new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(str).create()));

        for (Player p : Bukkit.getOnlinePlayers()) {
            if(p.hasPermission("Swear.Notify")){
                p.spigot().sendMessage(msg);
            }
        }

    }

    /**
     * Logs the message to console and a text file
     * @param player player to log the messag from
     * @param message the message to log
     */
    public void logWord(FPlayer player, String message){

        try {
            File file = new File(plugin.getDataFolder().getAbsolutePath()+"/SwearLogs.txt");
            file.createNewFile();

            FileWriter fw = new FileWriter(file, true);
            BufferedWriter bw = new BufferedWriter(fw);
            final String str = "[" + Calendar.getInstance().getTime() + "] [" + player.getChatMode().nicename + "] " + player.getName() + ": " +
                    message.replaceAll(String.valueOf(ChatColor.COLOR_CHAR), "&");
            Bukkit.getConsoleSender().sendMessage(ChatColor.RED+"Player Swear: \n\n"+str);
            bw.write(str);
            bw.newLine();
            fw.flush();
            bw.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

}
