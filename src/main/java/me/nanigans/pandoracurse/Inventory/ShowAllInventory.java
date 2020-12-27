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
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;
import java.util.stream.Collectors;

@FunctionalInterface
interface ShowMethods{
    void execute(ItemStack clicked);
}

public class ShowAllInventory implements Listener {
    private final Player player;
    private int page = 1;
    private Inventory inv;
    private boolean canDelete;
    private boolean switching = false;
    private final boolean canEdit;
    private final static PandoraCurse plugin = PandoraCurse.getPlugin(PandoraCurse.class);
    private final Map<Integer, ItemStack> items = new HashMap<Integer, ItemStack>(){{
        put(46, ItemUtils.createItem(Material.COMPASS, "Page Backwards", "METHOD~pageBack"));
        put(52, ItemUtils.createItem(Material.COMPASS, "Page Forward", "METHOD~pageForward"));
    }};
    private final Map<String, ShowMethods> methods = new HashMap<String, ShowMethods>(){{
       put("pageForward", ShowAllInventory.this::pageForward);
       put("pageBack", ShowAllInventory.this::pageBack);
       put("editWord", ShowAllInventory.this::editWord);
    }};

    public ShowAllInventory(Player player) {
        this.player = player;
        canEdit =  PandoraCurse.hasPermsTo(player, "Swear.Edit");
        canDelete = PandoraCurse.hasPermsTo(player, "Swear.Remove");
        this.inv = createInv();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        player.openInventory(inv);
    }

    @EventHandler
    public void onClick(InventoryClickEvent event){
        if(event.getClickedInventory() != null)
        if(event.getClickedInventory().equals(this.inv) && event.getWhoClicked().getUniqueId().equals(this.player.getUniqueId())){
            event.setCancelled(true);

            if(this.canDelete && event.getAction().toString().toLowerCase().contains("drop")) {
                onDrop(event);
                return;
            }
             onNormalClick(event);
        }
    }

    @EventHandler
    public void onCLose(InventoryCloseEvent event){
        if(event.getInventory().equals(this.inv)){
            if(event.getPlayer().getUniqueId().equals(this.player.getUniqueId())){
                if(!this.switching)
                HandlerList.unregisterAll(this);
            }
        }
    }


    /**
     * Handles all method clicks
     * @param event inv click event
     */
    public void onNormalClick(InventoryClickEvent event){

        ItemStack clicked = event.getCurrentItem();
        if (clicked != null) {
            final String method = NBTData.getNBT(clicked, "METHOD");
            if (method != null) {
                player.playSound(player.getLocation(), Sound.valueOf("CLICK"), 1, 1);
                if (methods.containsKey(method))
                    methods.get(method).execute(clicked);
            }
        }

    }

    public void editWord(ItemStack wordItem){
        if(canEdit){
            String word = wordItem.getItemMeta().getDisplayName();
            HandlerList.unregisterAll(this);
            final Map<String, Object> data = (Map<String, Object>) BlackListWords.getAllWords().get(word);
            new BlackListInventory(player, word, data);
        }
    }

    /**
     * Handles dropped items and will delete the swear from the list
     * @param event inv click event
     */
    public void onDrop(InventoryClickEvent event){
            BlackListWords.removeWord(event.getCurrentItem().getItemMeta().getDisplayName());
            event.getCurrentItem().setType(Material.AIR);
            final int slot = event.getSlot();
            event.getClickedInventory().setItem(slot, null);
    }

    /**
     * Page forward if next page exists
     * @param clicked item clicked
     */
    private void pageForward(ItemStack clicked){
        final int size = BlackListWords.getAllWords().size();
        int nextPage = (int) Math.min(Math.floor((double)size/45), page+1);
        if(nextPage > page){
            this.page = nextPage;
            swapInvs(createInv());
        }
    }

    /**
     * Page back if it exists
     * @param clicked item clicked
     */
    private void pageBack(ItemStack clicked){
        int nextPage = Math.max(1, this.page-1);
        if(nextPage < this.page){
            this.page = nextPage;
            swapInvs(createInv());

        }
    }

    private void swapInvs(Inventory to){
        this.switching = true;
        this.inv = to;
        player.openInventory(to);
        this.switching = false;
    }

    /**
     * Create a new inventory that shows all the swears
     * @return
     */
    private Inventory createInv(){

        final Inventory inv = Bukkit.createInventory(player, 54, "Blacklisted Swears");
        final Map<String, Object> allWords = BlackListWords.getAllWords();
        final List<Map.Entry<String, Object>> entries = allWords.entrySet().stream().collect(Collectors.toList());
        for (int i = page-1; i < Math.min(page*45, entries.size()); i++) {

            final Map<String, Object> data = (Map<String, Object>) entries.get(i).getValue();

            final ItemStack paper = ItemUtils.createItem(Material.PAPER, entries.get(i).getKey(), "METHOD~editWord");
            final ItemMeta meta = paper.getItemMeta();
            final Object useFuzzySet = data.get("useFuzzySet");
            meta.setLore(Arrays.asList(ChatColor.BLUE+"Added by: "+ChatColor.WHITE + data.get("added_by").toString().split(" - ")[1],
                    ChatColor.BLUE+"Alert Staff: "+ChatColor.WHITE+data.get("alertStaff"),
                    ChatColor.BLUE+"Higher Sensitivity: "+ChatColor.WHITE+data.get("sensitive"),
                    ChatColor.BLUE+"Fuzzy Match: "+ChatColor.WHITE+ useFuzzySet,
                    (Boolean.parseBoolean(useFuzzySet.toString()) ? ChatColor.BLUE+"Fuzzy Tolerance: "+ChatColor.WHITE+data.get("fuzzyTolerance")+"%" : null),
                    (this.canDelete ? ChatColor.DARK_PURPLE+"Press Q to delete" : null)));
            paper.setItemMeta(meta);
            inv.addItem(paper);

        }
        this.items.forEach(inv::setItem);

        return inv;
    }

}
