package me.nanigans.pandoracurse.Inventory;


import me.nanigans.pandoracurse.PandoraCurse;
import me.nanigans.pandoracurse.SwearWords.BlackListWords;
import me.nanigans.pandoracurse.Utils.ItemUtils;
import me.nanigans.pandoracurse.Utils.NBTData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
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
    private int oldTolerance = 0;
    private boolean switching = false;
    private boolean override = false;
    private final String bannedWord;
    private final static PandoraCurse plugin = PandoraCurse.getPlugin(PandoraCurse.class);
    private final Map<String, Methods> methods = new HashMap<String, Methods>(){{
        put("sensitivityToggle", BlackListInventory.this::sensitivityToggle);
        put("staffToggle", BlackListInventory.this::staffToggle);
        put("fuzzyToggle", BlackListInventory.this::fuzzyToggle);
        put("setToggle", BlackListInventory.this::setToggle);
        put("decreaseTolerance", BlackListInventory.this::decreaseTolerance);
        put("increaseTolerance", BlackListInventory.this::increaseTolerance);
        put("confirmFuzzy", BlackListInventory.this::confirmFuzzy);
        put("back", BlackListInventory.this::back);
        put("cancel", BlackListInventory.this::cancel);
        put("confirm", BlackListInventory.this::confirm);

    }};
    private final Map<Integer, ItemStack> items = new HashMap<Integer, ItemStack>(){{
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
        this.inv = createDefaultInv();
        player.openInventory(this.inv);
    }

    public BlackListInventory(Player player, String word, Map<String, Object> fromData){
        this.player = player;
        this.bannedWord = word;
        this.override = true;
        this.alertStaff = Boolean.parseBoolean(fromData.get("alertStaff").toString());
        this.fuzzySet = Boolean.parseBoolean(fromData.get("useFuzzySet").toString());
        if(this.fuzzySet)
        this.fuzzyTolerance = Integer.parseInt(fromData.get("fuzzyTolerance").toString());
        this.highSensitivity = Boolean.parseBoolean(fromData.get("sensitive").toString());
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        this.inv = createDefaultInv();
        player.openInventory(this.inv);
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
                }
            }
            event.setCancelled(true);

        }
    }

    @EventHandler
    public void onInvClose(InventoryCloseEvent event){
        if(event.getPlayer().getUniqueId().equals(this.player.getUniqueId())){
            if(event.getInventory().equals(this.inv) && !this.switching){
                HandlerList.unregisterAll(this);
            }
        }
    }

    private void confirm(InventoryClickEvent event){
        player.closeInventory();
        final BlackListWords blackListWords = new BlackListWords(this);
        if (!blackListWords.addWord(this.override)) {
            player.sendMessage(ChatColor.RED+"This word is already blacklisted");
            return;
        }
        player.playSound(player.getLocation(), Sound.valueOf("LEVEL_UP"), 1, 1);
        player.sendMessage(ChatColor.GREEN+"Swear word added!");
    }

    private void cancel(InventoryClickEvent event){
        player.closeInventory();
    }

    private void back(InventoryClickEvent event){
        this.fuzzySet = false;
        this.fuzzyTolerance = this.oldTolerance;
        Inventory inv = createDefaultInv();
        swapInventory(inv);
        this.fuzzyToggle(event);
    }

    private void confirmFuzzy(InventoryClickEvent event){
        this.fuzzySet = false;
        Inventory inv = createDefaultInv();
        swapInventory(inv);
        this.fuzzyToggle(event);
    }

    private void increaseTolerance(InventoryClickEvent event){
        final ItemStack item = inv.getItem(13);
        if(item != null){
            final ItemStack currentItem = event.getCurrentItem();
            final ItemMeta itemMeta = item.getItemMeta();
            this.fuzzyTolerance = Math.min(this.fuzzyTolerance+currentItem.getAmount(), 100);
            itemMeta.setDisplayName(ChatColor.DARK_PURPLE+""+this.fuzzyTolerance+"%");
            item.setItemMeta(itemMeta);
        }
    }

    private void decreaseTolerance(InventoryClickEvent event){

        final ItemStack item = inv.getItem(13);
        if(item != null){
            final ItemStack currentItem = event.getCurrentItem();
            final ItemMeta itemMeta = item.getItemMeta();
            this.fuzzyTolerance = Math.max(this.fuzzyTolerance-currentItem.getAmount(), 0);
            itemMeta.setDisplayName(ChatColor.DARK_PURPLE+""+this.fuzzyTolerance+"%");
            item.setItemMeta(itemMeta);
        }

    }

    private void setToggle(InventoryClickEvent event){

        this.oldTolerance = this.fuzzyTolerance;
        Inventory inv = Bukkit.createInventory(player, 27, "Set Tolearance");
        inv.setMaxStackSize(100);
        inv.setItem(9, ItemUtils.createItem("160/14", ChatColor.RED+"Decrease by 1", 1, "METHOD~decreaseTolerance"));
        inv.setItem(10, ItemUtils.createItem("160/14", ChatColor.RED+"Decrease by 5", 5, "METHOD~decreaseTolerance"));
        inv.setItem(11, ItemUtils.createItem("160/14", ChatColor.RED+"Decrease by 25", 25, "METHOD~decreaseTolerance"));
        inv.setItem(12, ItemUtils.createItem("160/14", ChatColor.RED+"Decrease by 50", 50, "METHOD~decreaseTolerance"));
        inv.setItem(13, ItemUtils.createItem(Material.PAPER, ChatColor.DARK_PURPLE+""+this.fuzzyTolerance+"%"));
        inv.setItem(14, ItemUtils.createItem("160/5", ChatColor.GREEN+"Increase by 50", 50, "METHOD~increaseTolerance"));
        inv.setItem(15, ItemUtils.createItem("160/5", ChatColor.GREEN+"Increase by 25", 25, "METHOD~increaseTolerance"));
        inv.setItem(16, ItemUtils.createItem("160/5", ChatColor.GREEN+"Increase by 5", 5, "METHOD~increaseTolerance"));
        inv.setItem(17, ItemUtils.createItem("160/5", ChatColor.GREEN+"Increase by 1", 1, "METHOD~increaseTolerance"));
        inv.setItem(21, ItemUtils.createItem(Material.BARRIER, ChatColor.RED+"Back", "METHOD~back"));
        inv.setItem(23, ItemUtils.createItem("160/13", ChatColor.GREEN+"Confirm", 1, "METHOD~confirmFuzzy"));

        swapInventory(inv);

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
            inv.setItem(31, diamond);

        }else{
            inv.setItem(31, null);
            item = ItemUtils.createItem("160/14", "Use Fuzzy Match", "METHOD~fuzzyToggle");
        }
        inv.setItem(22, item);
        items.replace(22, item);

    }

    private void staffToggle(InventoryClickEvent event){

        this.alertStaff = !this.alertStaff;
        ItemStack item;
        if(this.alertStaff)
            item = ItemUtils.createItem("160/5", "Alert Staff", "METHOD~staffToggle");
        else item = ItemUtils.createItem("160/14", "Alert Staff", "METHOD~staffToggle");
        inv.setItem(6, item);
        items.replace(6, item);
    }

    private void sensitivityToggle(InventoryClickEvent event){

        this.highSensitivity = !this.highSensitivity;
        ItemStack item;
        if(this.highSensitivity)
            item = ItemUtils.createItem("160/5", "Increased Sensitivity", "METHOD~sensitivityToggle");
        else item = ItemUtils.createItem("160/14", "Increased Sensitivity", "METHOD~sensitivityToggle");
        inv.setItem(2, item);
        items.replace(2, item);

    }

    public void swapInventory(Inventory toInv){
        this.switching = true;
        player.openInventory(toInv);
        this.inv = toInv;
        this.switching = false;
    }

    private Inventory createDefaultInv(){

        Inventory inv = Bukkit.createInventory(player, 45, "Add blacklisted word");
        items.forEach(inv::setItem);
        ItemStack word = ItemUtils.createItem(Material.PAPER, this.bannedWord);
        inv.setItem(13, word);
        return inv;

    }

    public Player getPlayer() {
        return player;
    }

    public boolean isAlertStaff() {
        return alertStaff;
    }

    public boolean isFuzzySet() {
        return fuzzySet;
    }

    public boolean isHighSensitivity() {
        return highSensitivity;
    }

    public int getFuzzyTolerance() {
        return fuzzyTolerance;
    }

    public String getBannedWord() {
        return bannedWord;
    }

    public static PandoraCurse getPlugin() {
        return plugin;
    }
}
