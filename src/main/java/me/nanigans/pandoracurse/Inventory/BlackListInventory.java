package me.nanigans.pandoracurse.Inventory;


import me.nanigans.pandoracurse.PandoraCurse;
import me.nanigans.pandoracurse.Utils.ItemUtils;
import me.nanigans.pandoracurse.Utils.NBTData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


@FunctionalInterface
interface Methods{
    void execute(InventoryClickEvent event);
}

public class BlackListInventory implements Listener {
    private final Player player;
    private Inventory inv;
    private boolean alertStaff = true;
    private boolean fuzzySet = false;
    private boolean highSensitivity = true;
    private int fuzzyTolerance = 0;
    private boolean switching = false;
    private final String bannedWord;
    private final static PandoraCurse plugin = PandoraCurse.getPlugin(PandoraCurse.class);
    private final Map<String, Methods> methods = new HashMap<String, Methods>(){{
        put("sensitivityToggle", BlackListInventory.this::sensitivityToggle);
        put("staffToggle", BlackListInventory.this::staffToggle);
        put("fuzzyToggle", BlackListInventory.this::fuzzyToggle);
    }};
    private final static Map<Integer, ItemStack> items = new HashMap<Integer, ItemStack>(){{
       put(2, ItemUtils.createItem("160/5", "Increased Sensitivity", "METHOD~sensitivityToggle"));
       put(6, ItemUtils.createItem("160/5", "Alert Staff", "METHOD~staffToggle"));
       put(13, ItemUtils.createItem(Material.PAPER, "Banned Word"));
       put(22, ItemUtils.createItem("160/14", "Use Fuzzy Match", "METHOD~fuzzyToggle"));//31 is diamond
       put(39, ItemUtils.createItem("160/14", ChatColor.RED+"Cancel", "METHOD~cancel"));
       put(41, ItemUtils.createItem("160/13", ChatColor.GREEN+"Confirm", "METHOD~confirm"));
    }};

    public BlackListInventory(Player player, String word){
        this.player = player;
        this.bannedWord = word;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        player.openInventory(createDefaultInv());
    }

    @EventHandler
    public void onClick(InventoryClickEvent event){
        if(event.getWhoClicked().getUniqueId().equals(this.player.getUniqueId())){

            final ItemStack item = event.getCurrentItem();
            if (item != null){
                final String method = NBTData.getNBT(item, "METHOD");
                if(method != null && this.methods.containsKey(method)){
                    player.playSound(player.getLocation(), Sound.valueOf("CLICK"), 1, 1);
                    this.methods.get(method).execute(event);
                    event.setCancelled(true);
                }
            }

        }
    }


    private void fuzzyToggle(InventoryClickEvent event){
        this.fuzzySet = !this.fuzzySet;
        ItemStack item;
        if(this.fuzzySet){
            item = ItemUtils.createItem("160/5", "Use Fuzzy Match", "METHOD~fuzzyToggle");
            ItemStack diamond = ItemUtils.createItem(Material.DIAMOND, "Tolerance", "METHOD~setToggle");
            ItemMeta meta = diamond.getItemMeta();
            meta.setLore(Collections.singletonList("Tolerance: " + this.fuzzyTolerance + "%"));
            diamond.setItemMeta(meta);
            event.getClickedInventory().setItem(31, diamond);

        }else{
            event.getClickedInventory().setItem(31, null);
            item = ItemUtils.createItem("160/14", "Use Fuzzy Match", "METHOD~fuzzyToggle");
        }
        event.getClickedInventory().setItem(22, item);
    }

    private void staffToggle(InventoryClickEvent event){

        this.alertStaff = !this.alertStaff;
        ItemStack item;
        if(this.alertStaff)
            item = ItemUtils.createItem("160/5", "Alert Staff", "METHOD~staffToggle");
        else item = ItemUtils.createItem("160/14", "Alert Staff", "METHOD~staffToggle");
        event.getClickedInventory().setItem(6, item);
    }

    private void sensitivityToggle(InventoryClickEvent event){

        this.highSensitivity = !this.highSensitivity;
        ItemStack item;
        if(this.highSensitivity)
            item = ItemUtils.createItem("160/5", "Increased Sensitivity", "METHOD~sensitivityToggle");
        else item = ItemUtils.createItem("160/14", "Increased Sensitivity", "METHOD~sensitivityToggle");
        event.getClickedInventory().setItem(2, item);

    }

    public void swapInventory(Inventory toInv){
        this.switching = true;
        player.openInventory(toInv);
        this.switching = false;
    }

    private Inventory createDefaultInv(){

        Inventory inv = Bukkit.createInventory(player, 45, "Add blacklisted word");
        items.forEach(inv::setItem);
        ItemStack word = ItemUtils.createItem(Material.PAPER, this.bannedWord);
        inv.setItem(13, word);
        return inv;

    }

}
